# SplitFriend
#### [COSC2657] Android Dev project for team Oatmeal_Cookie
s3768999-JungSanghwa**
</br>s3866724-KimMinsung
</br>s3912055-HanYeeun
</br>s3977947- Do Nhat Thanh

#### **[Distribution]**
25% for all members equally

## Functionalities
Basically, our app is an app for the users who want to calculate the money shared in their community. For example, it would be quite hard to calculate the amount of spent money when people go to travel or hold the party. Usually a person pay for all then seperate, but our app allows users to pay whatever they want to or able to. 
</br>In detail, user can gather in the group by sharing the code of it then create an activity for calculating. In the activity, user is allowed to add the list of bills as much as they want to then our app will calculate the total amount of the activity for them. When the activity is created, the user who create the activity would be the activity owner and other members who allocated in that activity will be assigned to pay for the calculated amount of money. Especially, our app is implemented with the "Stripe" SDK in order to allow users to pay directly to the activity owner. The status of the payment is also tracked in the activity detail. Furthermore, in the activity detail, the details of activity and its bills will be displayed in chart as well. Additionaly, the user needs to authorized the account with the email by checking real email.

#### **Features by member type**
[User]
1. Create Group
2. Find&Join Group
3. Create Activity
   * Enter the details of the activity with the bank info
   * Assign the members who will pay for this activity
   * Add the list of bills with its amount
5. [Payee] Check the status of others' payment
6. [Payer] Pay with Stripe

[Admin]
1. Read user list
2. Update user information

#### Test account for admin
id: admin
pw: 123123

## Techonology use
To manage the account of the users, Firebase Authentification is implemeneted.
To manage the data, Firebase Database is implemented.
To manage the payment, Stripe SDK is implemented with the server of NodeJS.

## Known issues  (can't fixed)



# Backend for paying with "Stripe"
## Node.js Server (Stripe Test Payment)
### Overview
This server is a Node.js + Express example for handling Stripe test payments.
When a client (Android, web, etc.) sends a payment request (amount, currency, etc.), the server creates a PaymentIntent via Stripe and returns its clientSecret.
* #### Key Feature
  * /create-payment-intent: Creates a Stripe PaymentIntent (in Test Mode) and returns the client_secret so the client can complete the payment.

### Tech Stack
* Node.js (v14 or higher recommended)
* Express
* Stripe (Test Mode)
* cors, dotenv, etc.

### Prerequisites
Node.js installed (check with node -v).

### Installation & Running

1. git clone https://github.com/minncode/stripeapi.git
2. cd stripe-test-server
3. npm install
4. node server.js

Once it’s running, you should see something like:
✅ Server is running on http://localhost:4242

Since this is a testing server, you only need to enter the card number 4242 4242 4242 4242 and provide any values for the remaining fields. You can also find other test card numbers in the Stripe documentation.

### Notes
Only Test Mode keys (sk_test_...) are used here, so no real money is charged.

