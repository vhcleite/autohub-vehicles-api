#!/bin/bash
echo "########### Criando recursos SNS/SQS no LocalStack ###########"

# Defina nomes e região (podem vir de env vars se quiser)
REGION="us-east-1"
ACCOUNT_ID="000000000000" # ID padrão do LocalStack
ENV="local"
SNS_TOPIC_NAME="AutoHubBusinessEventsTopic-$ENV"
VEHICLES_SALE_CREATED_QUEUE="VehiclesApi_SaleCreated_Queue-$ENV"
VEHICLES_SALE_CREATED_DLQ="VehiclesApi_SaleCreated_DLQ-$ENV"
CHARGES_VEHICLE_RESERVED_QUEUE="ChargesApi_VehicleReserved_Queue-$ENV"
CHARGES_VEHICLE_RESERVED_DLQ="ChargesApi_VehicleReserved_DLQ-$ENV"
SALES_EVENTS_QUEUE="SalesApi_Events_Queue-$ENV"
SALES_EVENTS_DLQ="SalesApi_Events_DLQ-$ENV"

# Espera LocalStack estar pronto (simples wait)
echo "Aguardando LocalStack ficar pronto..."
sleep 5 # Ajuste se necessário

# Criar Tópico SNS
echo "Criando Tópico SNS: $SNS_TOPIC_NAME"
awslocal sns create-topic --name $SNS_TOPIC_NAME --region $REGION
SNS_TOPIC_ARN="arn:aws:sns:$REGION:$ACCOUNT_ID:$SNS_TOPIC_NAME"
echo "SNS Topic ARN: $SNS_TOPIC_ARN"

# Função para criar Fila + DLQ
create_queue_with_dlq() {
  QUEUE_NAME=$1
  DLQ_NAME=$2

  echo "Criando DLQ: $DLQ_NAME"
  DLQ_URL=$(awslocal sqs create-queue --queue-name $DLQ_NAME --region $REGION --query QueueUrl --output text)
  DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url $DLQ_URL --attribute-names QueueArn --region $REGION --query Attributes.QueueArn --output text)
  echo "DLQ ARN: $DLQ_ARN"

  echo "Criando Fila Principal: $QUEUE_NAME"
  # Cria a política de redrive em JSON
  REDRIVE_POLICY=$(printf '{"deadLetterTargetArn":"%s","maxReceiveCount":"3"}' $DLQ_ARN)
  # Cria a fila principal associando a DLQ
  QUEUE_URL=$(awslocal sqs create-queue --queue-name $QUEUE_NAME --attributes RedrivePolicy=$REDRIVE_POLICY --region $REGION --query QueueUrl --output text)
  QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url $QUEUE_URL --attribute-names QueueArn --region $REGION --query Attributes.QueueArn --output text)
  echo "Queue ARN: $QUEUE_ARN"
  echo "Queue URL: $QUEUE_URL"

   # Cria política para permitir SNS enviar para esta fila
   POLICY=$(printf '{"Version":"2012-10-17","Statement":[{"Sid":"AllowSNSPublish-%s","Effect":"Allow","Principal":{"Service":"sns.amazonaws.com"},"Action":"sqs:SendMessage","Resource":"%s","Condition":{"ArnEquals":{"aws:SourceArn":"%s"}}}]}' $QUEUE_NAME $QUEUE_ARN $SNS_TOPIC_ARN)
   awslocal sqs set-queue-attributes --queue-url $QUEUE_URL --attributes Policy="$POLICY" --region $REGION
   echo "Política aplicada à fila $QUEUE_NAME"
}

# Criar as filas e DLQs
create_queue_with_dlq $VEHICLES_SALE_CREATED_QUEUE $VEHICLES_SALE_CREATED_DLQ
create_queue_with_dlq $CHARGES_VEHICLE_RESERVED_QUEUE $CHARGES_VEHICLE_RESERVED_DLQ
create_queue_with_dlq $SALES_EVENTS_QUEUE $SALES_EVENTS_DLQ
# Chame create_queue_with_dlq para outras filas...

# Criar Assinaturas SNS -> SQS (com filtros)
echo "Criando Assinaturas SNS -> SQS"
awslocal sns subscribe \
  --topic-arn $SNS_TOPIC_ARN \
  --protocol sqs \
  --endpoint $(awslocal sqs get-queue-attributes --queue-name $VEHICLES_SALE_CREATED_QUEUE --attribute-names QueueArn --query Attributes.QueueArn --output text) \
  --attributes '{"RawMessageDelivery":"true", "FilterPolicy":"{\"eventType\": [\"SaleCreated\"]}"}' \
  --region $REGION

 awslocal sns subscribe \
  --topic-arn $SNS_TOPIC_ARN \
  --protocol sqs \
  --endpoint $(awslocal sqs get-queue-attributes --queue-name $CHARGES_VEHICLE_RESERVED_QUEUE --attribute-names QueueArn --query Attributes.QueueArn --output text) \
  --attributes '{"RawMessageDelivery":"true", "FilterPolicy":"{\"eventType\": [\"VehicleReserved\"]}"}' \
  --region $REGION

 awslocal sns subscribe \
  --topic-arn $SNS_TOPIC_ARN \
  --protocol sqs \
  --endpoint $(awslocal sqs get-queue-attributes --queue-name $SALES_EVENTS_QUEUE --attribute-names QueueArn --query Attributes.QueueArn --output text) \
  --attributes '{"RawMessageDelivery":"true", "FilterPolicy":"{\"eventType\": [\"VehicleReservationFailed\", \"ChargeCreationFailed\", \"PaymentCompleted\", \"PaymentFailed\", \"ChargeExpired\"]}"}' \
  --region $REGION

echo "########### Recursos SNS/SQS criados no LocalStack ###########"
