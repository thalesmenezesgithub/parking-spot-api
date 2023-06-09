package com.api.parkingcontrol.controller;

import com.api.parkingcontrol.dto.ParkingSpotDTO;
import com.api.parkingcontrol.models.ParkingSpotModel;
import com.api.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    /*
     *   Save
     *   @parkingSpotDTO
     */
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

    /*
     * Lista todas as vagas de garagem cadastrada
     * Paginação de 10 ordenação ASC
     */
    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable)
    {
        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id)
    {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga do estacionamento não encontrado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotModelOptional.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteParkingSpot(@PathVariable(value = "id") UUID id)
    {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga do estacionamento não encontrado.");
        }

        parkingSpotService.delete(parkingSpotModelOptional.get());

        return ResponseEntity.status(HttpStatus.OK).body("Vaga de estacionamento deletado com sucesso!");
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateParkingSpot(@PathVariable(value = "id") UUID id,@RequestBody @Valid ParkingSpotDTO parkingSpotDto)
    {
        Optional<ParkingSpotModel> parkingSpotModelOptional = parkingSpotService.findById(id);

        if (!parkingSpotModelOptional.isPresent())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vaga do estacionamento não encontrado.");
        }

        var parkingSpotModel = parkingSpotModelOptional.get();
            parkingSpotModel.setParkingSpotNumber(parkingSpotDto.getParkingSpotNumber());
            parkingSpotModel.setLicensePlateCar(parkingSpotDto.getLicensePlateCar());
            parkingSpotModel.setModelCar(parkingSpotDto.getModelCar());
            parkingSpotModel.setBrandCar(parkingSpotDto.getBrandCar());
            parkingSpotModel.setColorCar(parkingSpotDto.getColorCar());
            parkingSpotModel.setResponsibleName(parkingSpotDto.getResponsibleName());
            parkingSpotModel.setApartment(parkingSpotDto.getApartment());
            parkingSpotModel.setBlock(parkingSpotDto.getBlock());

        return ResponseEntity.status(HttpStatus.OK).body(parkingSpotService.save(parkingSpotModel));

    }
}
