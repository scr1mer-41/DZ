#include <iostream>
#include <iomanip>
#include <unordered_map>
#include <string>
#include <vector>

#include "ovm.h"

OVM::OVM() : memory(MEMORY_SIZE, -1), pc(0), sp(MEMORY_SIZE), tickCounter(0) {}

void OVM::run()
{
    while (true)
    {
        int cmd = memory[pc];
        pc++;
        tickCounter++;

        if (cmd >= 0)
        {
            sp--;
            memory[sp] = cmd;
        }
        else if (cmd == OVM::STOP)
        {
            break;
        }
        else if (cmd == OVM::ADD)
        {
            sp++;
            memory[sp] = memory[sp] + memory[sp - 1];
        }
        else if (cmd == OVM::SUB)
        {
            sp++;
            memory[sp] = memory[sp] - memory[sp - 1];
        }
        else if (cmd == OVM::MULT)
        {
            sp++;
            memory[sp] = memory[sp] * memory[sp - 1];
        }
        else if (cmd == OVM::DIV)
        {
            sp++;
            memory[sp] = memory[sp] / memory[sp - 1];
        }
        else if (cmd == OVM::MOD)
        {
            sp++;
            memory[sp] = memory[sp] % memory[sp - 1];
        }
        else if (cmd == OVM::NEG)
        {
            memory[sp] = -memory[sp];
        }
        else if (cmd == OVM::LOAD)
        {
            memory[sp] = memory[memory[sp]]; 
        }
        else if (cmd == OVM::SAVE)
        {
            memory[memory[sp + 1]] = memory[sp];
            sp += 2;
        }
        else if (cmd == OVM::DUP)
        {
            memory[sp - 1] = memory[sp];
            sp--;
        }
        else if (cmd == OVM::DROP)
        {
            sp++;
        }
        else if (cmd == OVM::SWAP)
        {
            int tmp = memory[sp];
            memory[sp] = memory[sp + 1];
            memory[sp + 1] = tmp;
        }
        else if (cmd == OVM::OVER)
        {
            sp--;
            memory[sp] = memory[sp + 2];
        }
        else if (cmd == OVM::GOTO)
        {
            pc = memory[sp];
            sp++;
        }
        else if (cmd == OVM::IFEQ)
        {
            if (memory[sp + 2] == memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IFNE)
        {
            if (memory[sp + 2] != memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IFLT)
        {
            if (memory[sp + 2] < memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IFLE)
        {
            if (memory[sp + 2] <= memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IFGT)
        {
            if (memory[sp + 2] > memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IFGE)
        {
            if (memory[sp + 2] >= memory[sp + 1])
            {
                pc = memory[sp];
            }

            sp += 3;
        }
        else if (cmd == OVM::IN)
        {
            sp--;

            try
            {
                std::cout << "? ";
                std::cin >> memory[sp];
                // memory[sp] = 55;
            }
            catch (const std::exception& e)
            {
                std::cout << "������������ ����";
            }

        }
        else if (cmd == OVM::OUT)
        {
            std::cout << std::setw(memory[sp]) << memory[sp + 1];
            sp += 2;
        }
        else if (cmd == OVM::LN)
        {
            std::cout << std::endl;
        }
        else
        {
            std::cout << "������������ �������: " << cmd;
            break;
        }
    }

    std::cout << std::endl << "���������� ������: " << tickCounter << std::endl;
    if (sp < MEMORY_SIZE) // ���� ���� �� ����
    {
        std::cout << "��� ��������: " << memory[sp];
    }

}

void OVM::test_input()
{
    int i = 0;

    memory[i++] = 100;
    memory[i++] = OVM::IN;
    memory[i++] = OVM::SAVE;
    memory[i++] = 101;
    memory[i++] = 2;
    memory[i++] = OVM::SAVE;

    memory[i++] = 100;
    memory[i++] = OVM::LOAD;
    memory[i++] = 101;
    memory[i++] = OVM::LOAD;
    memory[i++] = OVM::MOD;
    memory[i++] = 0;
    memory[i++] = 22;
    memory[i++] = OVM::IFEQ;

    memory[i++] = 101;
    memory[i++] = 101;
    memory[i++] = OVM::LOAD;
    memory[i++] = 1;
    memory[i++] = OVM::ADD;
    memory[i++] = OVM::SAVE;

    memory[i++] = 6;
    memory[i++] = OVM::GOTO;

    memory[i++] = 101;
    memory[i++] = OVM::LOAD;
    memory[i++] = 100;
    memory[i++] = OVM::LOAD;
    memory[i++] = 34;
    memory[i++] = OVM::IFNE;

    memory[i++] = 100;
    memory[i++] = OVM::LOAD;
    memory[i++] = 0;
    memory[i++] = OVM::OUT;
    memory[i++] = 37;
    memory[i++] = OVM::GOTO;

    memory[i++] = 0;
    memory[i++] = 0;
    memory[i++] = OVM::OUT;

    memory[i++] = OVM::LN;
    memory[i++] = OVM::STOP;
}

std::vector<int>& OVM::getMemory()
{
    return memory;
}

std::string OVM::operation_to_string(int operation)
{
    auto it = operationToString.find(operation);
    if (it != operationToString.end()) {
        return it->second;
    }
    return "ERROR";
}

void OVM::printCode(int cmdCounter)
{
    for (int i = 0; i <= cmdCounter; i++)
    {
        std::cout << i << ") ";
        if (memory[i] >= 0)
        {
            std::cout << memory[i] << std::endl;
        }
        else
        {
            std::cout << operation_to_string(memory[i]) << std::endl;
        }
    }

}