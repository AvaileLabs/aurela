# Module Aurela Backend

The Aurela backend is a Spring Boot application for ingesting and exposing energy-market data.
It provides authenticated HTTP endpoints, an interactive Scalar API reference, and streaming
utilities for newline-delimited Energy Information Administration (EIA) petroleum datasets.

# Package com.availelabs.aurela

Contains the Spring Boot application entry point.

# Package com.availelabs.aurela.configuration

Contains web application routing configuration.

# Package com.availelabs.aurela.eia

Contains the EIA bulk-data HTTP API, petroleum record model, and streaming file reader.

# Package com.availelabs.aurela.security

Contains HTTP authentication and authorization configuration.