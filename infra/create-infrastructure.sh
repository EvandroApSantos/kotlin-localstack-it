#!/usr/bin/env bash

awslocal sqs create-queue --queue-name CAR_MAINTENANCE_INFO
echo "Queue CAR_MAINTENANCE_INFO created"

awslocal s3api create-bucket --bucket car-maintenance
echo "Bucket car-maintenance created"

echo "Localstack components created"

echo "DONE" > /tmp/result.txt
