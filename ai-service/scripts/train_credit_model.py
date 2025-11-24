#!/usr/bin/env python3
"""
Train Credit Score Model using RandomForest
"""
import pickle
from pathlib import Path
import numpy as np
from sklearn.ensemble import RandomForestClassifier

def train_credit_model():
    """Train and save Random Forest credit model"""
    
    # Generate synthetic credit data
    np.random.seed(42)
    
    # Features: [payment_history, credit_utilization, account_age, num_accounts]
    # Good credit (label 1): low utilization, old accounts, payment history
    good_credit = np.random.normal(loc=[90, 20, 10, 8], scale=[5, 10, 3, 2], size=(600, 4))
    good_labels = np.ones(600)
    
    # Bad credit (label 0): high utilization, young accounts, missed payments
    bad_credit = np.random.normal(loc=[40, 80, 2, 3], scale=[10, 10, 1, 2], size=(150, 4))
    bad_labels = np.zeros(150)
    
    # Combine data
    X = np.vstack([good_credit, bad_credit])
    y = np.hstack([good_labels, bad_labels])
    
    # Clamp feature values to realistic ranges
    X[:, 0] = np.clip(X[:, 0], 0, 100)  # payment history score
    X[:, 1] = np.clip(X[:, 1], 0, 100)  # credit utilization
    X[:, 2] = np.clip(X[:, 2], 0, 50)   # account age in years
    X[:, 3] = np.clip(X[:, 3], 1, 20)   # number of accounts
    
    # Train model
    model = RandomForestClassifier(n_estimators=100, random_state=42, max_depth=10)
    model.fit(X, y)
    
    # Save model
    models_path = Path(__file__).parent.parent / "models"
    models_path.mkdir(exist_ok=True)
    
    model_path = models_path / "credit_model.pkl"
    with open(model_path, 'wb') as f:
        pickle.dump(model, f)
    
    print(f"Credit model saved to {model_path}")

if __name__ == "__main__":
    train_credit_model()
