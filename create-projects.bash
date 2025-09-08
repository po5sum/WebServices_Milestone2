#!/usr/bin/env bash

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=customers-service \
--package-name=com.musicstore.customers \
--groupId=com.musicstore.customer \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
customers-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=musiccatalog-service \
--package-name=com.musicstore.musiccatalog \
--groupId=com.musicstore.musiccatalog \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
musiccatalog-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=orders-service \
--package-name=com.musicstore.orders \
--groupId=com.musicstore.orders \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
orders-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=storelocation-service \
--package-name=com.musicstore.storelocation \
--groupId=com.musicstore.storelocation \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
storelocation-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=api-gateway \
--package-name=com.musicstore.apigateway \
--groupId=com.musicstore.apigateway \
--dependencies=web,webflux,validation,hateoas \
--version=1.0.0-SNAPSHOT \
api-gateway

