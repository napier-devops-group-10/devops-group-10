package com.napier.sem;

/**
 * Represents a city from the World database.
 */
public class City {

    // City ID (primary key in the database)
    private int id;

    // City name
    private String name;

    // Country 3-letter code (foreign key linking to Country)
    private String countryCode;

    // District / administrative region
    private String district;

    // Total city population
    private int population;

    // --- Getters and Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }
}