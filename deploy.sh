#!/bin/bash

mvn clean package
kind load docker-image 
