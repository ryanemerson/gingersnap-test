#!/bin/bash

kind load docker-image \
  quay.io/gingersnap/cache-manager \
  quay.io/gingersnap/db-syncer \
  mysql:8.0.31 \
