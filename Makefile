SHELL := /bin/bash

SIM_UDID ?=
SIM_NAME ?= iPhone 17 Pro
SCHEME ?= iosApp
BUNDLE_ID ?= com.al4apps.chatshop.ChatShop
CONFIGURATION ?= Debug
DERIVED_DATA ?= build/ios
APP_NAME ?= ChatShop
OPEN_DEVICE ?= 1

IOS_ENV = \
  SIM_UDID='$(SIM_UDID)' \
  SIM_NAME='$(SIM_NAME)' \
  SCHEME='$(SCHEME)' \
  BUNDLE_ID='$(BUNDLE_ID)' \
  CONFIGURATION='$(CONFIGURATION)' \
  DERIVED_DATA='$(DERIVED_DATA)' \
  APP_NAME='$(APP_NAME)' \
  OPEN_DEVICE='$(OPEN_DEVICE)'

.PHONY: help ios ios-build ios-run ios-devices ios-clean

help:
	@echo "ChatShop"
	@echo ""
	@echo "  make ios                              собрать и запустить на симуляторе"
	@echo "  make ios-build                        только собрать"
	@echo "  make ios-run                          установить и запустить без сборки"
	@echo "  make ios-devices                      список доступных симуляторов"
	@echo "  make ios-clean                        удалить build/ios"
	@echo ""
	@echo "  make ios SIM_UDID=<udid>              конкретный симулятор"
	@echo "  make ios SIM_NAME='iPhone 17 Pro'     выбрать по имени"
	@echo "  make ios OPEN_DEVICE=0                не открывать DeviceHub"

ios:
	@$(IOS_ENV) ./scripts/ios.sh all

ios-build:
	@$(IOS_ENV) ./scripts/ios.sh build

ios-run:
	@$(IOS_ENV) ./scripts/ios.sh run

ios-devices:
	@./scripts/ios.sh devices

ios-clean:
	@rm -rf '$(DERIVED_DATA)'
	@echo "Удалено: $(DERIVED_DATA)"
