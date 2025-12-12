package com.dtvi.Pet.Ecommerce.service.interf;

import com.dtvi.Pet.Ecommerce.dto.Response;

public interface StatisticsService {

    Response getMonthlyStatistics(int month, int year);
}
