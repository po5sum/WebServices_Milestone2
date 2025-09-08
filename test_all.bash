#!/usr/bin/env bash
#
# Sample usage:
#   ./test_all.bash start stop
#   start and stop are optional
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
# When not in Docker
#: ${HOST=localhost}
#: ${PORT=7000}

# When in Docker
: ${HOST=localhost}
: ${PORT=8080}

#array to hold all our test data ids
allTestOrderIds=()
allTestArtistIds=()
allTestStoreLocationIds=()
allTestCustomerIds=()

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
      echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
      echo  "- Failing command: $curlCmd"
      echo  "- Response Body: $RESPONSE"
      exit 1
  fi
}

function assertCurl() {
  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]; then
    echo "Test OK (HTTP Code: $httpCode)"
  else
    echo "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode"
    echo "- Failing command: $curlCmd"
    echo "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {
  local expected=$1
  local actual=$2

  if [ "$expected" == "$actual" ]; then
    echo "Assertion OK: Expected '$expected', got '$actual'"
  else
    echo "Assertion FAILED: Expected '$expected', got '$actual'"
    exit 1
  fi
}

#have all the microservices come up yet?
function testUrl() {
    url=$@
    if curl $url -ks -f -o /dev/null
    then
          echo "Ok"
          return 0
    else
          echo -n "not yet"
          return 1
    fi;
}


#prepare the test data that will be passed in the curl commands for posts and puts
function setupTestdata() {
  # Generate a unique email address
  uniqueEmail="samantha_$(date +%s)@example.com"

  # CREATE SOME CUSTOMER TEST DATA
  body=$(cat <<EOF
{
   "firstName": "Samantha",
   "lastName": "Conner",
   "emailAddress": "$uniqueEmail",
   "phoneNumbers": [
       {
           "type": "HOME",
           "number": "514-555-0909"
       },
       {
           "type": "MOBILE",
           "number": "450-555-3456"
       }
   ],
   "streetAddress": "123 Main Street",
   "city": "Montreal",
   "province": "Quebec",
   "country": "Canada",
   "postalCode": "H1A 0A4"
}
EOF
)
  recreateCustomerAggregate 1 "$body"

  # CREATE SOME STORE LOCATION TEST DATA
  body='{
    "ownerName": "Michael Scott",
    "managerName": "Pam Beesly",
    "storeRating": 4.2,
    "phoneNumber": "514-123-4567",
    "email": "michael.scott@example.com",
    "openHours": "Mon-Fri 9:00AM - 6:00PM",
    "streetAddress": "1725 Slough Ave",
    "city": "Montreal",
    "province": "Quebec",
    "postalCode": "H2X 3L4"
  }'
  recreateStoreLocationAggregate 1 "$body"

  # CREATE SOME ARTIST TEST DATA
  body='{
    "artistName": "Radiohead",
    "country": "United Kingdom",
    "debutYear": 1985,
    "biography": "Radiohead is an English rock band known for experimental albums like OK Computer and Kid A."
  }'
  recreateArtistAggregate 1 "$body"

  # CREATE SOME ORDER TEST DATA
  body='{
    "artistId": "e5913a79-9b1e-4516-9ffd-06578e7af261",
    "artistName": "The Beatles",
    "albumId": "84c5f33e-8e5d-4eb5-b35d-79272355fa72",
    "albumTitle": "Abbey Road",
    "customerId": "c3540a89-cb47-4c96-888e-ff96708db4d8",
    "customerFirstName": "Alick",
    "customerLastName": "Ucceli",
    "storeId": "b2d3a4e7-f29b-4f5e-bf1c-8a77a7319a1e",
    "ownerName": "John Doe",
    "managerName": "Alice Smith",
    "orderDate": "2025-04-10",
    "orderStatus": "SHIPPED",
    "orderPrice": 29.99,
    "paymentMethod": "CREDIT_CARD"
  }'
  recreateOrderAggregate 1 "$body"

  # CREATE SOME ALBUM TEST DATA
  body='{
    "artistId": "'"${allTestArtistIds[1]}"'",
    "albumTitle": "OK Computer",
    "releaseDate": 1997,
    "genre": "Alternative Rock"
  }'
  recreateAlbumAggregate 1 "$body"
} #end setupTestdata

#RECREATE HELPERS
function recreateAlbumAggregate() {
  local testId=$1
  local aggregate=$2

  # Corrected path to match the expected API structure
  local response=$(curl -s -X POST http://$HOST:$PORT/api/v1/artists/${allTestArtistIds[$testId]}/albums \
    -H "Content-Type: application/json" --data "$aggregate")
  local albumId=$(echo "$response" | jq -r .albumId)

  if [ "$albumId" = "null" ] || [ -z "$albumId" ]; then
    echo "Failed to create album: $response"
    exit 1
  fi

  allTestAlbumIds[$testId]=$albumId
  echo "Added Album ID: $albumId"
}

function recreateCustomerAggregate() {
  local testId=$1
  local aggregate=$2
  local response=$(curl -s -X POST http://$HOST:$PORT/api/v1/customers \
    -H "Content-Type: application/json" --data "$aggregate")
  customerId=$(echo "$response" | jq -r .customerId)
  if [ "$customerId" = "null" ]; then
    echo "Failed to create customer: $response"
    exit 1
  fi
  allTestCustomerIds[$testId]=$customerId
  echo "Added Customer ID: $customerId"
}

function recreateStoreLocationAggregate() {
    local testId=$1
    local agg=$2

    # Generate a unique street address
    local uniqueStreetAddress="Unique Store St $(date +%s%N)"
    agg=$(echo "$agg" | jq --arg address "$uniqueStreetAddress" '.streetAddress = $address')

    # Send the POST request to create a store location
    local response=$(curl -s -X POST http://$HOST:$PORT/api/v1/stores \
      -H "Content-Type: application/json" --data "$agg")
    local storeId=$(echo "$response" | jq -r .storeId)

    # Validate the storeId
    if [ "$storeId" = "null" ] || [ -z "$storeId" ]; then
        echo "Failed to create store location: $response"
        exit 1
    fi

    # Store the valid storeId and log it
    allTestStoreLocationIds[$testId]=$storeId
    echo "Added StoreLocation ID: $storeId"
}
function recreateArtistAggregate() {
  local testId=$1
  local aggregate=$2

  # Generate a unique artist name
  local uniqueArtistName="Artist_$(date +%s%N)"
  aggregate=$(echo "$aggregate" | jq --arg name "$uniqueArtistName" '.artistName = $name')

  local response=$(curl -s -X POST http://$HOST:$PORT/api/v1/artists \
    -H "Content-Type: application/json" --data "$aggregate")
  local artistId=$(echo "$response" | jq -r .artistId)

  if [ "$artistId" = "null" ]; then
    echo "Failed to create artist: $response"
    exit 1
  fi

  allTestArtistIds[$testId]=$artistId
  echo "Added Artist ID: $artistId"
}
function recreateOrderAggregate() {
    local testId=$1
    local agg=$2
    local customerId=${allTestCustomerIds[$testId]}

    # Send the POST request to the correct endpoint
    local response=$(curl -s -X POST http://$HOST:$PORT/api/v1/customers/$customerId/orders \
      -H "Content-Type: application/json" --data "$agg")
    local orderId=$(echo "$response" | jq -r .orderId)

    # Check if the orderId is valid
    if [ "$orderId" = "null" ] || [ -z "$orderId" ]; then
        echo "Failed to create order: $response"
        exit 1
    fi

    # Store the valid orderId and log it
    allTestOrderIds[$testId]=$orderId
    echo "Added Order ID: $orderId"
}


#WAIT FOR SERVICE
function waitForService() {
    url=$@; n=0
    echo -n "Wait for: $url... "
    until testUrl $url; do
      ((n++)); if [[ $n == 100 ]]; then echo "Give up"; exit 1; fi
      sleep 6; echo -n ", retry #$n "
    done
    echo
}

#main
set -e

echo "HOST=${HOST}"; echo "PORT=${PORT}"
if [[ $@ == *"start"* ]]; then
  echo "Restarting environment..."; docker-compose down; docker-compose up -d
fi

waitForService http://$HOST:$PORT/api/v1/customers
setupTestdata


echo -e "\nTest 1: GET all customers"
assertCurl 200 "curl -s http://\$HOST:\$PORT/api/v1/customers"

echo -e "\nTest 2: GET customer by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}"
assertEqual "${allTestCustomerIds[1]}" "$(jq -r .customerId <<< "$RESPONSE")"

echo -e "\nTest 3: GET non-existing customer returns 404"
assertCurl 404 "curl -s http://$HOST:$PORT/api/v1/customers/00000000-0000-0000-0000-000000000000"

echo -e "\nTest 4: GET invalid customer returns 422"
assertCurl 422 "curl -s http://$HOST:$PORT/api/v1/customers/invalid!"

echo -e "\nTest 5: POST new customer"
uniqueEmail="test_$(date +%s)@example.com"
payload=$(cat <<EOF
{
  "firstName": "Test",
  "lastName": "User",
  "emailAddress": "$uniqueEmail",
  "contactMethodPreference": "EMAIL",
  "streetAddress": "1 Test St",
  "city": "TestCity",
  "province": "TestProv",
  "country": "Canada",
  "postalCode": "T1T1T1",
  "phoneNumbers": [{"type": "HOME", "number": "111-222-3333"}]
}
EOF
)
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers -H \"Content-Type: application/json\" --data '$payload'"
assertEqual "$uniqueEmail" "$(echo "$RESPONSE" | jq -r .emailAddress)"

echo -e "\nTest 6: PUT update customer"
upd=$(cat <<EOF
{
  "firstName": "Jotaro",
  "lastName": "KujoUpdated",
  "emailAddress": "unique_$(date +%s)@example.com",
  "contactMethodPreference": "TEXT",
  "streetAddress": "61 Farragut Street",
  "city": "Longueuil",
  "province": "Quebec",
  "country": "Canada",
  "postalCode": "L0P 1J8",
  "phoneNumbers": [{"type": "MOBILE", "number": "416-555-3333"}]
}
EOF
)
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]} -H \"Content-Type: application/json\" --data '$upd'"
assertEqual "KujoUpdated" "$(echo "$RESPONSE" | jq -r .lastName)"

# === ARTISTS ===
echo -e "\nTest 7: GET all artists"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/artists"

echo -e "\nTest 8: POST new artist"
uniqueArtistName="Artist_$(date +%s)"
payload=$(cat <<EOF
{
  "artistName": "$uniqueArtistName",
  "country": "United Kingdom",
  "debutYear": 1965,
  "biography": "This is a sample biography for the artist."
}
EOF
)
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/artists -H \"Content-Type: application/json\" --data '$payload'"
assertEqual "$uniqueArtistName" "$(echo "$RESPONSE" | jq -r .artistName)"


# === STORES ===
echo -e "\nTest 9: GET all stores"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/stores"

echo -e "\nTest 10: POST new store"
uniqueAddress="123 Store St $(date +%s)"
payload=$(cat <<EOF
{
  "ownerName": "Store Owner",
  "managerName": "Store Manager",
  "storeRating": 4.5,
  "phoneNumber": "123-456-7890",
  "email": "store@example.com",
  "openHours": "Mon-Fri 9:00AM - 5:00PM",
  "streetAddress": "$uniqueAddress",
  "city": "StoreCity",
  "province": "StoreProv",
  "postalCode": "S1S1S1"
}
EOF
)
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/stores -H \"Content-Type: application/json\" --data '$payload'"
assertEqual "$uniqueAddress" "$(echo "$RESPONSE" | jq -r .streetAddress)"

# === ORDERS ===
echo -e "\nTest 11: GET all orders for customer"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders"

echo -e "\nTest 12: GET order by ID"
assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders/${allTestOrderIds[1]}"
assertEqual "${allTestOrderIds[1]}" "$(jq -r .orderId <<< "$RESPONSE")"

#echo -e "\nTest 15: GET non-existing order returns 404"
#assertCurl 404 "curl -s http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders/f6dae904-b3ef-478a-9b52-c0c7223603a3"

echo "\nTest 13: GET invalid order returns 422"
assertCurl 422 "curl -s http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders/invalid!"

echo -e "\nTest 14: POST new order"
uniqueOrderDate=$(date +%Y-%m-%d)
payload=$(cat <<EOF
{
  "artistId": "${allTestArtistIds[1]}",
  "albumId": "84c5f33e-8e5d-4eb5-b35d-79272355fa72",
  "storeId": "${allTestStoreLocationIds[1]}",
  "orderDate": "$uniqueOrderDate",
  "orderStatus": "PENDING",
  "orderPrice": 19.99,
  "paymentMethod": "CREDIT_CARD"
}
EOF
)
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders -H \"Content-Type: application/json\" --data '$payload'"
assertEqual "PENDING" "$(echo "$RESPONSE" | jq -r .orderStatus)"

echo -e "\nTest 15: PUT update order"
upd5=$(cat <<EOF
{
  "artistId": "${allTestArtistIds[1]}",
  "albumId": "${allTestAlbumIds[1]}",
  "storeId": "${allTestStoreLocationIds[1]}",
  "orderDate": "$(date +%Y-%m-%d)",
  "orderStatus": "CANCELLED",
  "orderPrice": 19.99,
  "paymentMethod": "CREDIT_CARD"
}
EOF
)
assertCurl 200 "curl -s -X PUT http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders/${allTestOrderIds[1]} -H \"Content-Type: application/json\" --data '$upd5'"
assertEqual "CANCELLED" "$(jq -r .orderStatus <<< "$RESPONSE")"

echo -e "\nTest 16: DELETE order"
assertCurl 204 "curl -s -X DELETE http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders/${allTestOrderIds[1]}"

echo -e "\nTest 17: Aggregate invariant - orderPrice < 10 sets albumStatus to BARGAIN"
uniqueOrderDate=$(date +%Y-%m-%d)
payload=$(cat <<EOF
{
  "artistId": "${allTestArtistIds[1]}",
  "albumId": "${allTestAlbumIds[1]}",
  "storeId": "${allTestStoreLocationIds[1]}",
  "orderDate": "$uniqueOrderDate",
  "orderStatus": "PENDING",
  "orderPrice": 9.99,
  "paymentMethod": "CREDIT_CARD"
}
EOF
)
assertCurl 201 "curl -s -X POST http://$HOST:$PORT/api/v1/customers/${allTestCustomerIds[1]}/orders -H \"Content-Type: application/json\" --data '$payload'"

sleep 2

assertCurl 200 "curl -s http://$HOST:$PORT/api/v1/artists/${allTestArtistIds[1]}/albums/${allTestAlbumIds[1]}"
assertEqual "BARGAIN" "$(jq -r .status <<< "$RESPONSE")"




# Cleanup if requested
if [[ "$@" == *"stop"* ]]; then
  echo "Stopping the test environment..."
  echo "$ docker-compose down"
  docker-compose down
fi