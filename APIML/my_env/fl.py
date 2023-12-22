from flask import Flask, request, jsonify
from flask_cors import CORS 
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder

app = Flask(__name__)
CORS(app)

# Load the DataFrame
df = pd.read_csv('D:\Damini\devHom.csv')

# Material names
materials = ["area", "budget", "hall tiles", "hall paint", "hall ceiling", "main door", "bedroom floor",
             "bedroom ceiling", "bedroom paint", "bathroom tiles", "water closet"]

# Label Encoders for each material
label_encoders = {material: LabelEncoder().fit(df[material]) for material in materials}

# Model for each material
models = {material: RandomForestClassifier(n_estimators=100, random_state=42).fit(df[['budget', 'area']],
                                                                                   label_encoders[material].transform(
                                                                                       df[material]))
          for material in materials[2:]}

def predict(model, budget, area):
    new_input = {'budget': budget, 'area': area}
    new_data = pd.DataFrame([new_input])
    prediction = model.predict(new_data[['budget', 'area']])
    return prediction

def label_enc(mat, prediction):
    y = df[mat]
    label_encoder = LabelEncoder()
    y_encoded = label_encoder.fit_transform(y)
    predicted_structure = label_encoder.inverse_transform(prediction)
    return predicted_structure[0]

@app.route('/predict', methods=['POST'])
def get_predictions():
    data = request.get_json()

    budget = data.get('budget')
    area = data.get('area')

    if budget is None or area is None:
        return jsonify({"error": "Both 'budget' and 'area' are required."}), 400

    output = {}

    for material in materials[2:]:
        prediction = predict(models[material], budget, area)
        predicted_structure = label_enc(material, prediction)
        output[material.replace(" ", "_")] = predicted_structure
    print(output)
    return jsonify(output)



if __name__ == '__main__':
    app.run(debug=True)
