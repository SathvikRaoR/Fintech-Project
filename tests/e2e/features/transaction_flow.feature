Feature: Transaction Flow
  As a banking customer
  I want to perform transactions
  So that I can manage my accounts

  Scenario: Deposit increases balance
    Given I have an account with balance 1000.00
    When I deposit 500.00
    Then my new balance should be 1500.00
    And the transaction should be marked as NORMAL

  Scenario: Withdraw reduces balance
    Given I have an account with balance 1000.00
    When I withdraw 300.00
    Then my new balance should be 700.00
    And the transaction should be marked as NORMAL

  Scenario: Withdraw fails with overdraft
    Given I have an account with balance 500.00
    When I attempt to withdraw 1000.00
    Then the withdrawal should fail
    And I should see error message "Insufficient balance"

  Scenario: Transfer between accounts updates both
    Given I have an account with balance 1000.00
    And another account with balance 500.00
    When I transfer 200.00 from first to second account
    Then first account balance should be 800.00
    And second account balance should be 700.00
    And both should be marked as NORMAL risk

  Scenario: Large transfer is flagged as suspicious
    Given I have an account with balance 100000.00
    When I transfer 75000.00 to another account
    Then the transaction should be marked as SUSPICIOUS
    And AML rules should flag the transaction
