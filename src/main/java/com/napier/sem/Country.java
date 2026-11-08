package com.napier.sem;

/**
 * Represents a Country record from the world database.
 * Attributes are encapsulated with private access and getters/setters.
 */
public class Country {

    // 3-letter country code (Primary key)
    private String code;

    // Name of the country
    private String name;

    // Continent where the country is located
    private String continent;

    // Geographic region of the country
    private String region;

    // Surface area in square kilometers
    private double surfaceArea;

    // Total population
    private int population;

    // Capital city name
    private String capital;

    /**
     * Default constructor.
     */
    public Country() {
    }

    // --- Getters and Setters ---

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(double surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }
}