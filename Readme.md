To use this service locally, follow these steps:
1. Clone the repository to your local machine (git clone https://github.com/tssma/tech-assessment-credit-service.git)
2. Open the project in your preferred IDE.
3. Ensure you have Java 21 installed and your IDE is configured to use it.
4. Run the application using your IDE's run configuration or by executing the main class directly.
5. The service will start on the default port (usually 8080). You can access it via http://localhost:8080.
6. Use tools like Postman or curl to test the endpoint. Example curl command:
   curl -X GET http://localhost:8080/service/v1/loansByUser/11110001
7. To stop the service, terminate the process in your IDE or command line.


Assumptions:

1.) In the KBS schema definition in the presentation, it’s called “Owner” and is an object but in the actual data it’s called “owners” and is an array. => working with schema of actual data.
2.) In the given data, AmortisationPaymentAmount is written in PascalCase and is the only variable written in this form, but it's written in camelCase (amortisationPaymentAmount) in the user story -> used camelCase.
3.) currency and outstandingAmount for ParentLoan will only be returned if all Products for a Financing Object have the same currency
4.) Story S6 specifies interestDue as a date, but in the actual data it's a double / Decimal => assuming double
5.) limitAmount is specified as a double in the presentation but is a long in the actual data => assuming long
6.) amortisationAmountAnnual is specified as a double in the presentation but in the actual data it's a long => assuming long
7.) Assuming interestPaymentFrequency defines how often per year interest is paid
8.) Assuming defaultSettlementAccountNumber of the ParentLoan should be set to null
9.) S7: “the childLoans interestDue is set to interestRate = 2.5” => ??
10.) S2: GIVEN a financing object with two products "prodA" & "prodB"
AND "prodA" outstandingAmount = 120'000
AND "prodB" outstandingAmount = 85'000
=> there is no field "outstandingAmount" on Entity "Product", only "amount" => assuming "amount" is "outstandingAmount"
11.) S5: "...AND the parentLoan paymentFrequency = 4..." but the openAPI spec defines it as "Quarterly" => Keeping "4"
12.) S7: "...interestPaymentFrequency = 2 and interestRate = 1.2, interestPaymentFrequency = 6 respectively" but the openAPI spec defines it as "Quarterly" => Using "Quarterly" etc.
