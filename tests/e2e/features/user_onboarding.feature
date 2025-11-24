Feature: User Onboarding
  As a new user
  I want to register and login
  So that I can access my banking features

  Scenario: Successful registration
    Given I am on the registration page
    When I register with email "newuser@example.com" and password "SecurePass123" and name "New User"
    Then I should see registration successful message
    And the user should be created with KYC status "PENDING"

  Scenario: Successful login
    Given a user exists with email "john@example.com" and password "Password123"
    When I login with email "john@example.com" and password "Password123"
    Then I should receive a valid JWT token
    And token should expire in 3600 seconds

  Scenario: Login with invalid password
    Given a user exists with email "john@example.com"
    When I login with email "john@example.com" and password "WrongPassword"
    Then I should see an authentication error
