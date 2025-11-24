Feature: FinBot AI Insights
  As a user seeking financial advice
  I want to chat with FinBot
  So that I can get personalized financial guidance

  Scenario: Ask for saving advice
    Given I am logged in as user with id 1
    When I ask FinBot "How can I save 20% of my income?"
    Then I should receive saving advice
    And response should include "50/30/20" budgeting method
    And sources should be included

  Scenario: Ask for investment advice
    Given I am logged in as user with id 1
    When I ask FinBot "What are good investment options?"
    Then I should receive investment advice
    And response should mention diversification

  Scenario: Ask for credit advice
    Given I am logged in as user with id 1
    When I ask FinBot "How do I improve my credit score?"
    Then I should receive credit building advice
    And response should mention payment history

  Scenario: Fraud detection
    Given I have an account with balance 50000.00
    When I perform a transaction check for amount 1000.00
    Then fraud score should be returned
    And decision should be NORMAL or SUSPICIOUS
    And explanation should be provided

  Scenario: Credit score calculation
    Given I am user with id 1
    When I request my credit score
    Then I should receive a score between 300 and 850
    And probability of default should be returned
    And risk level should be LOW, MEDIUM, or HIGH
