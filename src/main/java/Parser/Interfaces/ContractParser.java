package Parser.Interfaces;

import Database.Models.Contract;

public interface ContractParser {
    Contract parse(String url);
}
