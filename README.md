# Posdata: Plataforma de Detección de Phishing para Mayores

![Status](https://img.shields.io/badge/Estado-En_Desarrollo-yellow)
![TFG](https://img.shields.io/badge/Tipo-TFG-blue)
![Python](https://img.shields.io/badge/BackEnd-Python-3776AB?logo=python&logoColor=white)
![Kotlin](https://img.shields.io/badge/FrontEnd-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![AWS](https://img.shields.io/badge/Cloud-AWS-232F3E?logo=amazon-aws&logoColor=white)

## Descripción del Proyecto

**Posdata** es un sistema diseñado para proteger a las personas mayores frente a ataques de phishing. El proyecto combina una aplicación móvil accesible con una arquitectura serverless en la nube capaz de analizar amenazas en tiempo real mediante Inteligencia Artificial.

Este repositorio contiene el código fuente, los modelos de IA y la documentación asociada al Trabajo de Fin de Grado.

## Arquitectura del Sistema

El sistema se divide en tres bloques fundamentales:

1.  **Frontend (Android):** Aplicación nativa desarrollada en Kotlin que intercepta notificaciones y SMS, priorizando la accesibilidad.
2.  **Backend (AWS Serverless):** Núcleo de procesamiento basado en AWS Lambda.
3.  **Inteligencia Artificial:** Modelo híbrido de detección de phishing.

## Estructura del Repositorio

Este repositorio sigue la siguiente estructura, por medio de la cual se centralizan todos los aspectos del TFG:

```text
posdata/
├── backend/          # Funciones Lambda y lógica de servidor (Python)
├── mobile-app/       # Código fuente de la aplicación Android (Kotlin)
├── ai-research/      # Notebooks de entrenamiento y datasets (Jupyter)
└── docs/             # Documentación, memoria del TFG y diagramas
