package com.dtvi.Pet.Ecommerce.service.interf;

import com.dtvi.Pet.Ecommerce.dto.AddressDto;
import com.dtvi.Pet.Ecommerce.dto.Response;


public interface AddressService {
    Response saveAndUpdateAddress(AddressDto addressDto);
}
