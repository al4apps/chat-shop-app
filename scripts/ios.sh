#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

ACTION="${1:-all}"
SCHEME="${SCHEME:-iosApp}"
BUNDLE_ID="${BUNDLE_ID:-com.al4apps.chatshop.ChatShop}"
CONFIGURATION="${CONFIGURATION:-Debug}"
DERIVED_DATA="${DERIVED_DATA:-$ROOT/build/ios}"
APP_NAME="${APP_NAME:-ChatShop}"
SIM_NAME="${SIM_NAME:-iPhone 17 Pro}"
SIM_UDID="${SIM_UDID:-}"
OPEN_DEVICE="${OPEN_DEVICE:-1}"

APP_PATH="$DERIVED_DATA/Build/Products/$CONFIGURATION-iphonesimulator/$APP_NAME.app"

fail() {
    echo "error: $*" >&2
    exit 1
}

usage() {
    cat <<'EOF'
Usage: scripts/ios.sh <all|build|run|devices>

Environment:
  SIM_UDID        concrete simulator UDID (highest priority)
  SIM_NAME        simulator name when none is booted (default: iPhone 17 Pro)
  SCHEME          Xcode scheme (default: iosApp)
  BUNDLE_ID       app bundle id (default: com.al4apps.chatshop.ChatShop)
  CONFIGURATION   Debug/Release (default: Debug)
  DERIVED_DATA    derived data path (default: build/ios)
  APP_NAME        built app name (default: ChatShop)
  OPEN_DEVICE     1 — open DeviceHub/Simulator after launch (default: 1)
EOF
}

require_xcode() {
    command -v xcodebuild >/dev/null 2>&1 || {
        fail "xcodebuild не найден. Установи Xcode и выполни: sudo xcode-select -s /Applications/Xcode.app/Contents/Developer"
    }
}

find_booted_iphone() {
    xcrun simctl list devices booted \
        | awk '/iPhone/ && /Booted/ { print; exit }'
}

find_available_by_name() {
    xcrun simctl list devices available \
        | grep -F " $SIM_NAME (" \
        | head -n 1 || true
}

extract_udid() {
    grep -oE '[0-9A-Fa-f-]{36}' | head -n 1
}

resolve_udid() {
    if [[ -n "$SIM_UDID" ]]; then
        xcrun simctl list devices available | grep -Fq "$SIM_UDID" \
            || fail "Симулятор с UDID '$SIM_UDID' не найден. Список: make ios-devices"
        printf '%s\n' "$SIM_UDID"
        return
    fi

    local booted
    booted="$(find_booted_iphone)"
    if [[ -n "$booted" ]]; then
        printf '%s\n' "$booted" | extract_udid
        return
    fi

    local available
    available="$(find_available_by_name)"
    if [[ -z "$available" ]]; then
        echo "Нет booted iPhone и не найден симулятор '$SIM_NAME'." >&2
        echo "Доступные устройства:" >&2
        xcrun simctl list devices available >&2
        exit 1
    fi
    printf '%s\n' "$available" | extract_udid
}

ensure_booted() {
    local udid="$1"
    if xcrun simctl list devices booted | grep -Fq "$udid"; then
        return
    fi

    echo "Загружаю симулятор $udid..."
    xcrun simctl boot "$udid" 2>/dev/null || true
    xcrun simctl bootstatus "$udid" -b
}

build_app() {
    local udid="$1"
    echo "Собираю $SCHEME ($CONFIGURATION) для симулятора $udid..."
    xcodebuild \
        -project "$ROOT/iosApp/iosApp.xcodeproj" \
        -scheme "$SCHEME" \
        -configuration "$CONFIGURATION" \
        -sdk iphonesimulator \
        -destination "platform=iOS Simulator,id=$udid" \
        -derivedDataPath "$DERIVED_DATA" \
        build \
        CODE_SIGNING_ALLOWED=NO

    [[ -d "$APP_PATH" ]] || fail "Сборка завершилась, но приложение не найдено: $APP_PATH"
}

run_app() {
    local udid="$1"
    [[ -d "$APP_PATH" ]] || fail "Приложение не собрано: $APP_PATH. Сначала выполни: make ios-build"

    echo "Устанавливаю $APP_NAME..."
    xcrun simctl terminate "$udid" "$BUNDLE_ID" >/dev/null 2>&1 || true
    xcrun simctl install "$udid" "$APP_PATH"

    echo "Запускаю $BUNDLE_ID..."
    xcrun simctl launch "$udid" "$BUNDLE_ID"

    if [[ "$OPEN_DEVICE" == "1" ]]; then
        open -a DeviceHub >/dev/null 2>&1 \
            || open -a Simulator >/dev/null 2>&1 \
            || true
    fi

    echo
    echo "Готово."
    echo "  Simulator: $udid"
    echo "  App:       $APP_PATH"
    echo "  Bundle:    $BUNDLE_ID"
}

require_xcode

case "$ACTION" in
    devices)
        xcrun simctl list devices available
        ;;
    build)
        udid="$(resolve_udid)"
        ensure_booted "$udid"
        build_app "$udid"
        ;;
    run)
        udid="$(resolve_udid)"
        ensure_booted "$udid"
        run_app "$udid"
        ;;
    all)
        udid="$(resolve_udid)"
        ensure_booted "$udid"
        build_app "$udid"
        run_app "$udid"
        ;;
    help|-h|--help)
        usage
        ;;
    *)
        usage >&2
        exit 2
        ;;
esac
