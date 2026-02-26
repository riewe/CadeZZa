package by.riewe.cadence.ui.screens.previews

import by.riewe.cadence.data.entity.CadenzzaEntity

val sampleCadenzzas = listOf(
    CadenzzaEntity(
        id = 2,
        cadenzzaNumber = "2",
        startDate = 1760425200L,
        startTime = 0L,
        driver1 = "John Doe",
        driver2 = "Vin Diesel",
        endDate = null,
        endTime = null,
        truckNumber = "ABC 123",
        startTrailerNumber = "XYZ 789",
        endTrailerNumber = null,
        startOdometer = 100000,
        endOdometer = null,
        startTruckFuel = 500,
        endTruckFuel = null,
        startTrailerFuel = 0,
        endTrailerFuel = null,
        startMH = 100,
        endMH = null,
        totalMileage = 0,
        totalDays = 0
    ),
    CadenzzaEntity(
        id = 1,
        cadenzzaNumber = "1",
        startDate = 1760425200,
        startTime = 0L,
        driver1 = "Jane Smith",
        driver2 = "Jacky Chan",
        endDate = 1767027780,
        endTime = 1767099999,
        truckNumber = "DEF 456",
        startTrailerNumber = "UVW 123",
        endTrailerNumber = "FY673",
        startOdometer = 200000,
        endOdometer = 256487,
        startTruckFuel = 600,
        endTruckFuel = 854,
        startTrailerFuel = 240,
        endTrailerFuel = 125,
        startMH = 200,
        endMH = 645,
        totalMileage = 56487,
        totalDays = 0
    )
)
