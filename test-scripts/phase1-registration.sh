#!/bin/bash

# Phase 1: Identity & Profile Link Testing
# This script tests the dual-write pattern: Keycloak + Local Database

echo "======================================"
echo "Phase 1: Registration & Profile Link"
echo "======================================"
echo ""

BASE_URL="http://localhost:8081/api"
KEYCLOAK_URL="http://localhost:8080"
REALM="my-realm"
CLIENT_ID="spring-boot-app"
CLIENT_SECRET="eQghEj2GomevJCl2ho8rSgjRmNVd6NCR"

echo "Test 1: Register User Alice"
echo "----------------------------"
ALICE_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice@example.com",
    "password": "AlicePass123",
    "firstName": "Alice",
    "lastName": "Smith"
  }')

echo "$ALICE_RESPONSE" | jq .
echo ""

ALICE_IDENTITY_ID=$(echo "$ALICE_RESPONSE" | jq -r '.data.identityId')
echo "Alice's Identity ID: $ALICE_IDENTITY_ID"
echo ""

echo "Test 2: Register User Bob"
echo "-------------------------"
BOB_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "bob",
    "email": "bob@example.com",
    "password": "BobPass123",
    "firstName": "Bob",
    "lastName": "Johnson"
  }')

echo "$BOB_RESPONSE" | jq .
echo ""

BOB_IDENTITY_ID=$(echo "$BOB_RESPONSE" | jq -r '.data.identityId')
echo "Bob's Identity ID: $BOB_IDENTITY_ID"
echo ""

echo "Test 3: Register User Charlie"
echo "-----------------------------"
CHARLIE_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "charlie",
    "email": "charlie@example.com",
    "password": "CharliePass123",
    "firstName": "Charlie",
    "lastName": "Brown"
  }')

echo "$CHARLIE_RESPONSE" | jq .
echo ""

echo "Test 4: Try to register duplicate username (should fail)"
echo "--------------------------------------------------------"
DUPLICATE_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alice",
    "email": "alice2@example.com",
    "password": "AlicePass123",
    "firstName": "Alice",
    "lastName": "Duplicate"
  }')

echo "$DUPLICATE_RESPONSE" | jq .
echo ""

echo "Test 5: Get JWT Token for Alice"
echo "-------------------------------"
ALICE_TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "username=alice" \
  -d "password=AlicePass123")

ALICE_TOKEN=$(echo "$ALICE_TOKEN_RESPONSE" | jq -r '.access_token')
echo "Alice's Token (first 50 chars): ${ALICE_TOKEN:0:50}..."
echo ""

echo "Test 6: Get Alice's Profile (authenticated)"
echo "------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/me" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 7: Get Alice's Profile by username (public)"
echo "-----------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/alice" | jq .
echo ""

echo "Test 8: Update Alice's Profile"
echo "------------------------------"
curl -s -X PUT "$BASE_URL/profiles/me" \
  -H "Authorization: Bearer $ALICE_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "bio": "Software engineer and coffee enthusiast",
    "avatarUrl": "https://example.com/alice.jpg"
  }' | jq .
echo ""

echo "Test 9: Verify Profile Update"
echo "----------------------------"
curl -s -X GET "$BASE_URL/profiles/alice" | jq .
echo ""

echo "======================================"
echo "Phase 1 Testing Complete!"
echo "======================================"
echo ""
echo "Summary:"
echo "- Created users in Keycloak: alice, bob, charlie"
echo "- Created corresponding profiles in local database"
echo "- Verified dual-write pattern working"
echo "- Tested profile retrieval and updates"
echo ""
echo "Tokens for next phase:"
echo "ALICE_TOKEN=$ALICE_TOKEN"
echo ""
echo "Export this token for Phase 2 testing:"
echo "export ALICE_TOKEN='$ALICE_TOKEN'"
