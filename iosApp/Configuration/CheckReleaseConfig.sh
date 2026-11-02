#!/bin/sh
set -eu

if [ "${CONFIGURATION:-}" != "Release" ]; then
    exit 0
fi

if [ -z "${API_BASE_URL:-}" ]; then
    echo "error: API_BASE_URL_RELEASE is required for Release builds." >&2
    echo "Set it in Xcode user-defined settings or pass API_BASE_URL_RELEASE=https://<host>/api/v1." >&2
    exit 1
fi

case "${API_BASE_URL}" in
    https://*) ;;
    *)
        echo "error: API_BASE_URL must use https:// in Release." >&2
        exit 1
        ;;
esac

case "${API_BASE_URL}" in
    *localhost*|*10.0.2.2*)
        echo "error: API_BASE_URL must not point to a development host." >&2
        exit 1
        ;;
esac
