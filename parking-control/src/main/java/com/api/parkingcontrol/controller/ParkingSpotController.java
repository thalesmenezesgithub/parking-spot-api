package com.api.parkingcontrol.controller;

import com.api.parkingcontrol.dto.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController
{
    final ParkingSpotService parkingSpotService;

    public ParkingSpotController(ParkingSpotService parkingSpotService)
    {
        this.parkingSpotService = parkingSpotService;
    }

    @PostMapping
    public ResponseEntity<Object> saveParkingSpot(@RequestBody @Valid ParkingSpotDTO parkingSpotDTO)
    {
        if(parkingSpotService.existsByLicensePlateCar(parkingSpotDTO.getLicensePlateCar()))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito: A placa do carro está sendo utilizado na vaga!");
        }
        if(parkingSpotService.existsByParkingSpotNumber(parkingSpotDTO.getParkingSpotNumber()))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito: A vaga de estacionamento está em uso!");
        }
        if(parkingSpotService.existsByApartmentAndBlock(parkingSpotDTO.getApartment(), parkingSpotDTO.getBlock()))
        {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflito: Vaga de estacionamento está cadastrada neste apartamento/bloco!");
        }

        var parkingSpotModel = new ParkingSpotModel();
        BeanUtils.copyProperties(parkingSpotDTO, parkingSpotModel);
        parkingSpotModel.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));

        return ResponseEntity.status(HttpStatus.CREATED).body(parkingSpotService.save(parkingSpotModel)); //Fazer
    }

}
