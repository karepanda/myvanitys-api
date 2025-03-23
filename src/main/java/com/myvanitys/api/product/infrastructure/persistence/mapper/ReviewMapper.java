package com.myvanitys.api.product.infrastructure.persistence.mapper;

import com.myvanitys.api.product.domain.Review;
import com.myvanitys.api.product.infrastructure.persistence.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {ProductMapper.class, EntityIdMapper.class})
public interface ReviewMapper {

  ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

  Review toDomain(ReviewEntity entity);

  ReviewEntity toEntity(Review domain);
}