package com.vipul.ecommerce.dto;

import com.vipul.ecommerce.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    private OrderStatus status;
}