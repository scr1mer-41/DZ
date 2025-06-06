#pragma once

#include <iostream>
#include <unordered_map>
#include <string>
#include <vector>

class Item
{
public:
    enum class ItemTypes
    {
        Integer,
        Boolean
    };

    std::string name;
    std::string value;
    std::string addr;
    ItemTypes type;
    std::string typeOfItem;
};