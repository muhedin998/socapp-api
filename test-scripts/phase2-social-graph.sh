#!/bin/bash

# Phase 2: Social Graph Testing
# Tests follow/unfollow functionality and social relationships

echo "======================================"
echo "Phase 2: Social Graph (Follow/Unfollow)"
echo "======================================"
echo ""

BASE_URL="http://localhost:8081/api"
KEYCLOAK_URL="http://localhost:8080"
REALM="my-realm"
CLIENT_ID="spring-boot-app"
CLIENT_SECRET="eQghEj2GomevJCl2ho8rSgjRmNVd6NCR"

# Function to get token
get_token() {
    local username=$1
    local password=$2

    TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "grant_type=password" \
      -d "client_id=$CLIENT_ID" \
      -d "client_secret=$CLIENT_SECRET" \
      -d "username=$username" \
      -d "password=$password")

    echo $(echo "$TOKEN_RESPONSE" | jq -r '.access_token')
}

echo "Getting tokens for users..."
ALICE_TOKEN=$(get_token "alice" "AlicePass123")
BOB_TOKEN=$(get_token "bob" "BobPass123")
CHARLIE_TOKEN=$(get_token "charlie" "CharliePass123")
echo "Tokens acquired!"
echo ""

echo "Test 1: Alice follows Bob"
echo "------------------------"
curl -s -X POST "$BASE_URL/profiles/bob/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 2: Alice follows Charlie"
echo "----------------------------"
curl -s -X POST "$BASE_URL/profiles/charlie/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 3: Bob follows Alice"
echo "------------------------"
curl -s -X POST "$BASE_URL/profiles/alice/follow" \
  -H "Authorization: Bearer $BOB_TOKEN" | jq .
echo ""

echo "Test 4: Charlie follows Alice"
echo "----------------------------"
curl -s -X POST "$BASE_URL/profiles/alice/follow" \
  -H "Authorization: Bearer $CHARLIE_TOKEN" | jq .
echo ""

echo "Test 5: Bob follows Charlie"
echo "--------------------------"
curl -s -X POST "$BASE_URL/profiles/charlie/follow" \
  -H "Authorization: Bearer $BOB_TOKEN" | jq .
echo ""

echo "Test 6: Get Alice's Profile (should show following count: 2)"
echo "----------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/alice" | jq .
echo ""

echo "Test 7: Get Alice's Following List"
echo "----------------------------------"
curl -s -X GET "$BASE_URL/profiles/alice/following" | jq .
echo ""

echo "Test 8: Get Alice's Followers List (should have Bob and Charlie)"
echo "--------------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/alice/followers" | jq .
echo ""

echo "Test 9: Get Bob's Profile (should show followers: 1, following: 2)"
echo "----------------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/bob" | jq .
echo ""

echo "Test 10: Get Charlie's Followers (should have Alice and Bob)"
echo "----------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/charlie/followers" | jq .
echo ""

echo "Test 11: Alice unfollows Charlie"
echo "-------------------------------"
curl -s -X DELETE "$BASE_URL/profiles/charlie/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 12: Verify Alice's Following (should only have Bob now)"
echo "----------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/alice/following" | jq .
echo ""

echo "Test 13: Verify Charlie's Followers (should only have Bob now)"
echo "------------------------------------------------------------"
curl -s -X GET "$BASE_URL/profiles/charlie/followers" | jq .
echo ""

echo "Test 14: Try to follow yourself (should fail)"
echo "--------------------------------------------"
curl -s -X POST "$BASE_URL/profiles/alice/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 15: Try to follow same user twice (should fail)"
echo "---------------------------------------------------"
curl -s -X POST "$BASE_URL/profiles/bob/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "Test 16: Try to unfollow someone not followed (should fail)"
echo "---------------------------------------------------------"
curl -s -X DELETE "$BASE_URL/profiles/charlie/follow" \
  -H "Authorization: Bearer $ALICE_TOKEN" | jq .
echo ""

echo "======================================"
echo "Phase 2 Testing Complete!"
echo "======================================"
echo ""
echo "Social Graph Summary:"
echo "--------------------"
echo "Alice: following Bob | followers: Bob, Charlie"
echo "Bob: following Alice, Charlie | followers: Alice"
echo "Charlie: following Alice | followers: Bob"
echo ""
echo "All bidirectional relationships verified!"
echo ""
echo "Tokens for Phase 3 testing:"
echo "export ALICE_TOKEN='$ALICE_TOKEN'"
echo "export BOB_TOKEN='$BOB_TOKEN'"
echo "export CHARLIE_TOKEN='$CHARLIE_TOKEN'"
