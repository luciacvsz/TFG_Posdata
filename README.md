# Posdata: Plataforma de Detección de Phishing para Mayores

![Status](https://img.shields.io/badge/Estado-En_Desarrollo-yellow)
![TFG](https://img.shields.io/badge/Tipo-TFG-blue)
![Python](https://img.shields.io/badge/Backend-Python-3776AB?logo=python&logoColor=white)
![Kotlin](https://img.shields.io/badge/Mobile-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![AWS](https://img.shields.io/badge/Cloud-AWS-232F3E?logo=amazon-aws&logoColor=white)

## 📖 Descripción del Proyecto

**Posdata** es un sistema integral diseñado para proteger a las personas mayores frente a ataques de phishing (SMS y notificaciones fraudulentas). El proyecto combina una aplicación móvil accesible con una arquitectura serverless en la nube capaz de analizar amenazas en tiempo real mediante Inteligencia Artificial.

Este repositorio contiene el código fuente, los modelos de IA y la documentación asociada al Trabajo de Fin de Grado (TFG).

## 🏗️ Arquitectura del Sistema

El sistema se divide en tres bloques fundamentales:

1.  **Frontend (Android):** Aplicación nativa desarrollada en Kotlin que intercepta notificaciones y SMS, priorizando la accesibilidad.
2.  **Backend (AWS Serverless):** Núcleo de procesamiento basado en AWS Lambda, API Gateway y DynamoDB.
3.  **Inteligencia Artificial:** Modelo híbrido de detección de phishing optimizado con TFLite.

*(Aquí podrás insertar tu diagrama de arquitectura de Draw.io más adelante)*

## 🛠️ Stack Tecnológico

### Backend & Cloud ☁️
* **Lenguaje:** Python 3.x
* **Compute:** AWS Lambda (Serverless)
* **API:** Amazon API Gateway
* **Base de Datos:** Amazon DynamoDB (NoSQL)
* **Almacenamiento:** Amazon S3 (Artefactos y modelos)
* **Notificaciones:** Amazon SNS

### Inteligencia Artificial 🧠
* **Entrenamiento:** Google Colab (Jupyter Notebooks)
* **Librerías:** TensorFlow, Keras, TextBlob, Pandas, Scikit-learn
* **Inferencia:** TensorFlow Lite (TFLite) para optimización en entornos limitados.

### Frontend (Mobile) 📱
* **OS:** Android
* **Lenguaje:** Kotlin
* **IDE:** Android Studio
* **UI:** Jetpack Compose (Enfoque en accesibilidad)
* **Networking:** Retrofit + Coroutines

### Herramientas de Apoyo 🔧
* **Diseño UI/UX:** Figma
* **Documentación:** LaTeX (Overleaf)
* **Diagramas:** Draw.io

## 📂 Estructura del Repositorio

Este repositorio sigue una estructura de **Monorepo** para centralizar todos los aspectos del TFG:

```text
posdata/
├── backend/          # Funciones Lambda y lógica de servidor (Python)
├── mobile-app/       # Código fuente de la aplicación Android (Kotlin)
├── ai-research/      # Notebooks de entrenamiento y datasets (Jupyter)
└── docs/             # Documentación, memoria del TFG y diagramas