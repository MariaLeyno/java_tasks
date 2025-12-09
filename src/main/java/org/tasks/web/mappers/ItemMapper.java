package org.tasks.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.tasks.model.Item;
import org.tasks.web.dto.ItemDTO;

import java.util.List;
import java.util.Map;

@Mapper
public interface ItemMapper {
    ItemMapper INSTANCE = Mappers.getMapper(ItemMapper.class);

    ItemDTO fromEntityToDto(Item item);
    Item fromDtoToEntity(ItemDTO itemDTO);

    List<ItemDTO> fromEntitiesToDtoList(List<Item> itemList);

    ItemDTO fromMapToDto(Map<String, String> map);
}
