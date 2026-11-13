package com.napier.sem;

/**
 * Represents a Country record from the world database.
 * Fields are encapsulated using private access modifiers with getters and setters.
 */
public class Country {

    // Unique 3-letter country code (Primary Key, e.g., 'ESP')
    private String code;

    // Full country name (e.g., 'Spain')
    private String name;

    // Continent name where the country is located
    private String continent;

    // Geographic sub-region
    private String region;

    // Total land surface area in square kilometres
    private double surfaceArea;

    // Total population count
    private int population;

    // Name of the capital city (obtained via JOIN with the city table)
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