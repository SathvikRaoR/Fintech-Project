#!/usr/bin/env python3
"""
Train Isolation Forest model for anomaly detection
"""
import pickle
from pathlib import Path
import numpy as np
from sklearn.ensemble import IsolationForest

def train_anomaly_model():
    """Train and save Isolation Forest model"""
    
    # Generate synthetic transaction data
    np.random.seed(42)
    
    # Normal transactions
    normal_transactions = np.random.normal(loc=5000, scale=2000, size=(800, 4))
    
    # Abnormal transactions (high amounts)
    abnormal_transactions = np.random.normal(loc=50000, scale=10000, size=(50, 4))
    
    # Combine data
    X = np.vstack([normal_transactions, abnormal_transactions])
    
    # Train model
    model = IsolationForest(contamination=0.05, random_state=42)
    model.fit(X)
    
    # Save model
    models_path = Path(__file__).parent.parent / "models"
    models_path.mkdir(exist_ok=True)
    
    model_path = models_path / "isolation_forest.pkl"
    with open(model_path, 'wb') as f:
        pickle.dump(model, f)
    
    print(f"Anomaly detection model saved to {model_path}")

if __name__ == "__main__":
    train_anomaly_model()
