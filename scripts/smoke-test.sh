#!/usr/bin/env bash
# [Benjamin - #08, 2026-10-15] Test de fumee apres deploiement (utilise par la CI et en local) :
# verifie que l'application deployee par docker compose repond correctement de bout en bout
# (nginx -> backend -> PostgreSQL, et ecriture du journal d'audit dans MongoDB).
# Usage : BASE_URL=http://localhost:8081 bash scripts/smoke-test.sh
set -u
BASE_URL="${BASE_URL:-http://localhost:8081}"
FAILED=0

check() { # libelle, code attendu, code obtenu
  if [ "$2" = "$3" ]; then echo "  OK   $1 ($3)"; else echo "  FAIL $1 : attendu $2, obtenu $3"; FAILED=1; fi
}

echo "Attente de l'application sur $BASE_URL ..."
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/" || true)
  [ "$code" = "200" ] && break
  sleep 3
done

echo "Tests :"
check "Page d'accueil Angular" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/")"
check "Route Angular profonde (fallback nginx)" 200 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/promotions")"
check "API sans token refusee" 401 "$(curl -s -o /dev/null -w '%{http_code}' "$BASE_URL/api/filieres")"
check "Login mauvais mot de passe" 401 "$(curl -s -o /dev/null -w '%{http_code}' -H 'Content-Type: application/json' \
  -d '{"username":"admin@educmanager.fr","password":"faux"}' "$BASE_URL/api/auth/login")"

TOKEN=$(curl -s -H 'Content-Type: application/json' -d '{"username":"admin@educmanager.fr","password":"admin123"}' \
  "$BASE_URL/api/auth/login" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
if [ -n "$TOKEN" ]; then echo "  OK   Login admin (token recu)"; else echo "  FAIL Login admin"; FAILED=1; fi

check "Lecture des filieres (PostgreSQL)" 200 "$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/filieres")"
check "Lecture des promotions" 200 "$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/promotions")"

sleep 2
AUDIT=$(curl -s -H "Authorization: Bearer $TOKEN" "$BASE_URL/api/audit?action=LOGIN_SUCCESS")
if echo "$AUDIT" | grep -q '"totalElements":[1-9]'; then echo "  OK   Journal d'audit ecrit dans MongoDB"; else echo "  FAIL Journal d'audit : $AUDIT"; FAILED=1; fi

HEADERS=$(curl -s -D - -o /dev/null "$BASE_URL/")
if echo "$HEADERS" | grep -qi 'content-security-policy'; then echo "  OK   En-tetes de securite nginx"; else echo "  FAIL En-tetes de securite"; FAILED=1; fi

if [ "$FAILED" = "0" ]; then echo "=== Test de fumee : SUCCES ==="; else echo "=== Test de fumee : ECHEC ==="; fi
exit $FAILED
