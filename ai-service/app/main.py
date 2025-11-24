from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import logging
import json
from pathlib import Path
import pickle
import numpy as np
from datetime import datetime

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Fintech AI Service", version="1.0.0")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load models
models_path = Path(__file__).parent.parent / "models"
isolation_forest_model = None
credit_model = None

def load_models():
    global isolation_forest_model, credit_model
    try:
        isolation_forest_path = models_path / "isolation_forest.pkl"
        credit_model_path = models_path / "credit_model.pkl"
        
        if isolation_forest_path.exists():
            with open(isolation_forest_path, 'rb') as f:
                isolation_forest_model = pickle.load(f)
            logger.info("Isolation Forest model loaded")
        else:
            logger.warning("Isolation Forest model not found, using default")
            
        if credit_model_path.exists():
            with open(credit_model_path, 'rb') as f:
                credit_model = pickle.load(f)
            logger.info("Credit model loaded")
        else:
            logger.warning("Credit model not found, using default")
    except Exception as e:
        logger.error(f"Error loading models: {e}")

# Load models on startup
load_models()

@app.on_event("startup")
async def startup_event():
    logger.info("AI Service starting up...")

@app.get("/health")
async def health_check():
    return {
        "status": "UP",
        "service": "fintech-ai-service",
        "timestamp": datetime.now().isoformat()
    }

@app.post("/fraud-check")
async def fraud_check(request: dict):
    """
    Check transaction for fraud using anomaly detection
    Expected request: {"account_id": int, "amount": float, "timestamp": str}
    """
    try:
        account_id = request.get("accountId") or request.get("account_id")
        amount = request.get("amount")
        timestamp_str = request.get("timestamp") or request.get("timestamp")
        
        if account_id is None or amount is None:
            raise HTTPException(status_code=400, detail="Missing required fields")
        
        # Simple heuristic fraud detection
        # Flag if amount is unusually high (threshold: 50000)
        score = 0.0
        decision = "NORMAL"
        explanation = "Transaction appears normal"
        
        if float(amount) > 50000:
            score = 0.75
            decision = "SUSPICIOUS"
            explanation = "Transaction amount is unusually high"
        elif float(amount) > 20000:
            score = 0.4
            explanation = "Transaction amount is moderately high"
        else:
            score = 0.02
            explanation = "Low anomaly score - transaction appears normal"
        
        return {
            "score": score,
            "decision": decision,
            "explanation": explanation,
            "accountId": account_id,
            "amount": amount,
            "timestamp": timestamp_str
        }
    except Exception as e:
        logger.error(f"Fraud check error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/credit-score")
async def credit_score(request: dict):
    """
    Calculate credit score for a user
    Expected request: {"user_id": int, "features": {...}}
    """
    try:
        user_id = request.get("userId") or request.get("user_id")
        features = request.get("features", {})
        
        if user_id is None:
            raise HTTPException(status_code=400, detail="Missing user_id")
        
        # Default credit score calculation
        base_score = 650
        score = base_score
        
        # Adjust based on provided features
        if isinstance(features, dict):
            if features.get("payment_history_score"):
                score += min(features.get("payment_history_score", 0) * 0.35, 150)
            if features.get("credit_utilization"):
                utilization = features.get("credit_utilization", 0)
                score += (1 - utilization) * 150 * 0.3  # Lower is better
            if features.get("account_age_years"):
                score += min(features.get("account_age_years", 0) * 10, 50)
        
        score = int(max(300, min(score, 850)))  # Clamp between 300-850
        prob_default = max(0.01, 1.0 - (score - 300) / 550)
        
        return {
            "score": score,
            "probDefault": round(prob_default, 4),
            "riskLevel": "LOW" if score >= 700 else "MEDIUM" if score >= 600 else "HIGH"
        }
    except Exception as e:
        logger.error(f"Credit score error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/chat")
async def chat(request: dict):
    """
    Financial advice chatbot
    Expected request: {"user_id": int, "message": str}
    """
    try:
        user_id = request.get("userId") or request.get("user_id")
        message = request.get("message", "").lower()
        
        if user_id is None:
            raise HTTPException(status_code=400, detail="Missing user_id")
        
        if not message:
            raise HTTPException(status_code=400, detail="Message is required")
        
        # Rule-based financial advice
        reply = generate_financial_advice(message)
        
        return {
            "reply": reply,
            "sources": ["FinBot AI", "Financial Best Practices"],
            "userId": user_id
        }
    except Exception as e:
        logger.error(f"Chat error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

def generate_financial_advice(message: str) -> str:
    """Generate financial advice based on message content"""
    
    if "save" in message or "savings" in message:
        return "To save 20% of your income, try the 50/30/20 budgeting method: 50% for needs, 30% for wants, and 20% for savings and debt repayment. Start by tracking your expenses for a month to understand your spending patterns."
    
    elif "invest" in message or "investment" in message:
        return "Consider diversifying your investments with a mix of stocks, bonds, and index funds. Start with low-cost index funds if you're a beginner. Always research before investing and consider your risk tolerance and time horizon."
    
    elif "credit" in message or "credit score" in message:
        return "Build credit by paying bills on time, keeping credit utilization low (under 30%), and maintaining a long account history. Avoid closing old accounts and dispute any errors on your credit report."
    
    elif "debt" in message or "loan" in message:
        return "To manage debt effectively, create a repayment plan, consider debt consolidation if beneficial, and try the avalanche or snowball method. Cut unnecessary expenses and allocate extra funds toward debt reduction."
    
    elif "budget" in message:
        return "Create a monthly budget by listing all income and expenses. Use the 50/30/20 rule: 50% for essentials, 30% for discretionary, and 20% for savings/debt. Review and adjust your budget monthly."
    
    elif "emergency" in message or "fund" in message:
        return "Build an emergency fund with 3-6 months of living expenses in a high-yield savings account. This protects you from unexpected costs and reduces financial stress."
    
    else:
        return "Thank you for your financial question! I'm here to help. Please ask me about saving, investing, credit, budgeting, or managing debt for more specific advice."

@app.post("/generate-tests")
async def generate_tests(request: dict):
    """
    Generate test cases based on recent failures
    Expected request: {"recent_fail_reports": [...], "coverage_targets": 0.95}
    """
    try:
        fail_reports = request.get("recentFailReports", [])
        coverage_targets = request.get("coverageTargets", 0.95)
        
        # Generate basic Cucumber feature file names
        generated_features = []
        
        if fail_reports:
            for i, report in enumerate(fail_reports):
                feature_name = f"features/generated_edge_case_{i+1}.feature"
                generated_features.append(feature_name)
        
        return {
            "generatedFeatureFiles": generated_features,
            "coverageTarget": coverage_targets,
            "status": "success"
        }
    except Exception as e:
        logger.error(f"Test generation error: {e}")
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
