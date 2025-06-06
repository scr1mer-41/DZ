#pragma once

#include "Item.h"

#include <unordered_map>
#include <iostream>
#include <string>
#include <vector>


class TableOfName
{
public:
    void openScope();
    void closeScope();
    void addItem(const Item& item);
    void newItem(const Item& item);
    Item* findItem(std::string);
    std::vector<std::reference_wrapper<Item>> getVars();

    Item moduleItem(std::string name);
    Item constItem(std::string name, Item::ItemTypes type, std::string value);
    Item varItem(std::string name, Item::ItemTypes type, std::string addr);
    Item typeItem(std::string name, Item::ItemTypes type);
    Item functionItem(std::string name, Item::ItemTypes type);
    Item procedureItem(std::string name);

private:
    std::vector<std::unordered_map<std::string, Item>> table;
};