# SplitFriend
[COSC2657] Android Dev project for team Oatmeal_Cookie





Node.js Server (Stripe Test Payment)

## Overview

This server is a Node.js + Express example for handling Stripe test payments.
When a client (Android, web, etc.) sends a payment request (amount, currency, etc.), the server creates a PaymentIntent via Stripe and returns its clientSecret.
* ### Key Feature
  * /create-payment-intent: Creates a Stripe PaymentIntent (in Test Mode) and returns the client_secret so the client can complete the payment.

## Tech Stack
* Node.js (v14 or higher recommended)
* Express
* Stripe (Test Mode)
* cors, dotenv, etc.

## Prerequisites
Node.js installed (check with node -v).

## Installation & Running

1. git clone https://github.com/minncode/stripeapi.git
2. cd stripe-test-server
3. npm install
4. node server.js

Once it’s running, you should see something like:
✅ Server is running on http://localhost:4242

## Notes
Only Test Mode keys (sk_test_...) are used here, so no real money is charged.

