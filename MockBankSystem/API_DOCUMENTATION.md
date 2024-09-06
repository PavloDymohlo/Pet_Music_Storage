# API Documentation

## Endpoint: /transaction

- **Method:** POST
- **URL:** http://localhost:8081/transaction
- **Description:** Executes a transaction to transfer funds from one card to another.

### Request:

- **Content-Type:** application/json
- **Request body (TransactionDTO):**
  - `outputCardNumber` (long): The card number of the sender.
  - `targetCardNumber` (long): The card number of the recipient.
  - `sum` (int): The amount to be transferred.
  - `cardExpirationDate` (string): The expiration date of the card in the format MM/yy.
  - `cvv` (short): The CVV code of the sender's card.

#### Request Example:

```json
{
    "outputCardNumber": 1234567890123456,
    "targetCardNumber": 6543210987654321,
    "sum": 100,
    "cardExpirationDate": "12/23",
    "cvv": 123
}
Response:
HTTP Status:
200 OK for a successful transaction
400 Bad Request for invalid data.
Response body:
If the transaction is successful: "Transaction successful!"
If there is an error (e.g., insufficient funds or invalid data): "Transaction failed: {message}"
Response Examples:
Successful transaction:

json
Copy code
"Transaction successful!"
Failed transaction:

json
Copy code
"Transaction failed: Insufficient funds!"
HTTP Response Statuses:
200 OK: The transaction was completed successfully.
400 Bad Request: Invalid input data or an error occurred during the transaction.