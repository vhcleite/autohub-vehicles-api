package com.fiap.autohub.autohub_vehicles_api.domain.services;

import com.fiap.autohub.autohub_vehicles_api.domain.commands.CreateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.commands.UpdateVehicleCommand;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.Vehicle;
import com.fiap.autohub.autohub_vehicles_api.domain.entities.VehicleStatus;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.OptimisticLockingException;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.VehicleNotFoundException;
import com.fiap.autohub.autohub_vehicles_api.domain.exceptions.VehicleUpdateForbiddenException;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.in.VehicleServicePort;
import com.fiap.autohub.autohub_vehicles_api.domain.ports.out.VehicleRepositoryPort;
import com.fiap.autohub.autohub_vehicles_api.infrastructure.persistence.entities.VehiclePersistenceEntity;
import jakarta.persistence.OptimisticLockException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service // Indica que é um bean de serviço Spring
public class VehicleServiceImpl implements VehicleServicePort {

    private static final Logger logger = LoggerFactory.getLogger(VehicleServiceImpl.class);

    private final VehicleRepositoryPort vehicleRepository;

    public VehicleServiceImpl(VehicleRepositoryPort vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional // Garante atomicidade na criação
    public Vehicle createVehicle(CreateVehicleCommand command, String ownerId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        Vehicle newVehicle = new Vehicle(
                null,
                command.make(),
                command.model(),
                command.year(),
                command.color(),
                command.price(),
                command.description(),
                VehicleStatus.AVAILABLE, // Status inicial
                ownerId, // ID do dono vindo do Principal
                0L, // Versão inicial
                now,
                now
        );
        return vehicleRepository.save(newVehicle);
    }

    @Override
//    @Transactional // Garante atomicidade na atualização
    public Vehicle updateVehicle(UUID id, UpdateVehicleCommand command, String ownerId) {
        // Busca o veículo garantindo que pertence ao ownerId correto
        Vehicle existingVehicle = vehicleRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id + " for owner: " + ownerId));

        // Verifica se o veículo pode ser alterado (não pode estar vendido ou reservado)
        if (existingVehicle.status() != VehicleStatus.AVAILABLE) {
            throw new VehicleUpdateForbiddenException("Vehicle cannot be updated when status is " + existingVehicle.status());
        }

        // Verifica Optimistic Locking
        if (!Objects.equals(existingVehicle.version(), command.expectedVersion())) {
            throw new OptimisticLockingException("Vehicle version mismatch. Expected: " + command.expectedVersion() + ", Found: " + existingVehicle.version());
        }

        // Cria um NOVO objeto Vehicle com os dados atualizados (Records são imutáveis)
        Vehicle updatedVehicle = new Vehicle(
                existingVehicle.id(),
                command.make() != null ? command.make() : existingVehicle.make(),
                command.model() != null ? command.model() : existingVehicle.model(),
                command.year() != null ? command.year() : existingVehicle.year(),
                command.color() != null ? command.color() : existingVehicle.color(),
                command.price() != null ? command.price() : existingVehicle.price(),
                command.description() != null ? command.description() : existingVehicle.description(),
                existingVehicle.status(),
                existingVehicle.ownerId(),
                existingVehicle.version(), // JPA/Hibernate incrementará automaticamente (@Version)
                existingVehicle.createdAt(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        try {
            return vehicleRepository.save(updatedVehicle);
        } catch (OptimisticLockException ex) {
            throw new OptimisticLockingException("Failed to update vehicle due to concurrent modification.", ex);
        }
    }

    @Override
    public Optional<Vehicle> findVehicleById(UUID id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public Page<Vehicle> findAvailableVehicles(Specification<VehiclePersistenceEntity> spec, Pageable pageable) {
        Specification<VehiclePersistenceEntity> availableSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("status"), VehicleStatus.AVAILABLE));
        return vehicleRepository.findAll(availableSpec, pageable);
    }

    @Override
    public Page<Vehicle> findSoldVehicles(Specification<VehiclePersistenceEntity> spec, Pageable pageable) {
        Specification<VehiclePersistenceEntity> soldSpec = Specification.where(spec)
                .and((root, query, cb) -> cb.equal(root.get("status"), VehicleStatus.SOLD));
        return vehicleRepository.findAll(soldSpec, pageable);
    }

    @Override
    public List<Vehicle> findVehiclesByOwner(String ownerId) {
        return vehicleRepository.findByOwnerId(ownerId);
    }


    @Override
    @Transactional
    public void deleteVehicleLogically(UUID id, String ownerId) {
        Vehicle vehicleToDelete = vehicleRepository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle not found with id: " + id + " for owner: " + ownerId));

        if (vehicleToDelete.status() != VehicleStatus.AVAILABLE) {
            throw new VehicleUpdateForbiddenException("Vehicle cannot be deleted when status is " + vehicleToDelete.status());
        }

        vehicleRepository.deleteLogically(vehicleToDelete);
    }

    @Override
    @Transactional
    public Vehicle reserveVehicle(UUID id, String saleId) {
        logger.info("Attempting to reserve vehicle {} for sale {}", id, saleId);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle " + id + " not found for reservation by sale " + saleId));

        if (vehicle.status() != VehicleStatus.AVAILABLE) {
            logger.warn("Reservation failed for vehicle {}: not available (status: {})", id, vehicle.status());
            throw new VehicleUpdateForbiddenException("Vehicle " + id + " is not available for reservation. Status: " + vehicle.status());
        }

        Vehicle reservedVehicle = new Vehicle(
                vehicle.id(), vehicle.make(), vehicle.model(), vehicle.year(), vehicle.color(), vehicle.price(), vehicle.description(),
                VehicleStatus.RESERVED, // Muda status
                vehicle.ownerId(), vehicle.version(), // Passa a versão atual
                vehicle.createdAt(), OffsetDateTime.now(ZoneOffset.UTC)
        );
        try {
            Vehicle saved = vehicleRepository.save(reservedVehicle); // Tenta salvar com a versão atual
            logger.info("Vehicle {} reserved successfully for sale {}", id, saleId);
            // TODO: Publicar evento VehicleReserved (com saleId, vehicleId) para o SNS/EventBridge
            // Ex: eventPublisher.publish(new VehicleReservedEvent(saved.id(), saleId));
            return saved;
        } catch (OptimisticLockException ex) {
            logger.warn("Optimistic lock exception while reserving vehicle {}", id, ex);
            throw new OptimisticLockingException("Failed to reserve vehicle due to concurrent modification.", ex);
        }
    }

    @Override
    @Transactional
    public Vehicle markVehicleAsSold(UUID id) {
        logger.info("Attempting to mark vehicle {} as SOLD", id);
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException("Vehicle " + id + " not found to mark as sold."));

        if (vehicle.status() != VehicleStatus.RESERVED) {
            throw new VehicleUpdateForbiddenException("Vehicle " + id + " cannot be marked as sold from status " + vehicle.status());
        }

        Vehicle soldVehicle = new Vehicle(
                vehicle.id(), vehicle.make(), vehicle.model(), vehicle.year(), vehicle.color(), vehicle.price(), vehicle.description(),
                VehicleStatus.SOLD,
                vehicle.ownerId(), vehicle.version(),
                vehicle.createdAt(), OffsetDateTime.now(ZoneOffset.UTC)
        );
        try {
            Vehicle saved = vehicleRepository.save(soldVehicle);
            logger.info("Vehicle {} marked as SOLD successfully", id);
            // TODO: Publicar evento VehicleSold (com vehicleId, ownerId, etc.) para o SNS/EventBridge
            return saved;
        } catch (OptimisticLockException ex) {
            logger.warn("Optimistic lock exception while marking vehicle {} as sold.", id, ex);
            throw new OptimisticLockingException("Failed to mark vehicle as sold due to concurrent modification.", ex);
        }
    }

    @Override
    @Transactional
    public void unreserveVehicle(UUID id) {
        logger.info("Attempting to unreserve vehicle {}", id);
        Vehicle vehicle = vehicleRepository.findById(id).orElse(null);

        if (vehicle != null && vehicle.status() == VehicleStatus.RESERVED) {
            Vehicle availableVehicle = new Vehicle(
                    vehicle.id(), vehicle.make(), vehicle.model(), vehicle.year(), vehicle.color(), vehicle.price(), vehicle.description(),
                    VehicleStatus.AVAILABLE,
                    vehicle.ownerId(), vehicle.version(),
                    vehicle.createdAt(), OffsetDateTime.now(ZoneOffset.UTC)
            );
            try {
                vehicleRepository.save(availableVehicle);
                logger.info("Vehicle {} unreserved successfully.", id);
                // TODO: Publicar evento VehicleReservationCancelled? (Opcional)
            } catch (OptimisticLockException ex) {
                logger.error("CRITICAL: Failed to unreserve vehicle {} due to concurrent modification during compensation.", id, ex);
                throw new OptimisticLockingException("Failed to unreserve vehicle during compensation.", ex);
            }
        } else if (vehicle != null) {
            logger.warn("Attempted to unreserve vehicle {} which was not in RESERVED state (current: {}). Skipping compensation.", id, vehicle.status());
        } else {
            logger.warn("Attempted to unreserve vehicle {} which was not found. Skipping compensation.", id);
        }
    }
}