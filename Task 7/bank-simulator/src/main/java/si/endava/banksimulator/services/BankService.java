package si.endava.banksimulator.services;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import si.endava.banksimulator.dtos.BankDTO;
import si.endava.banksimulator.entities.Bank;
import si.endava.banksimulator.feignClient.PaymentNetworkFeignClientInterface;
import si.endava.banksimulator.mappers.BankMapper;
import si.endava.banksimulator.repositories.BankRepository;

@Service
@RequiredArgsConstructor
public class BankService {

    private final BankRepository bankRepository;
    private final BankMapper bankMapper;

    private final PaymentNetworkFeignClientInterface paymentNetworkFeignClientInterface;

    public List<BankDTO> getAllBanks() {
        return bankRepository.findAll().stream()
                .map(bankMapper::bankToBankDTO)
                .collect(Collectors.toList());
    }

    public BankDTO findByUuid(UUID uuid) {
        return bankMapper.bankToBankDTO(
                bankRepository
                        .findBankByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Bank with uuid " + uuid + " not exists.")));
    }

    public Bank findByBic(String bic) {
        return bankRepository
                .findBankByBic(bic)
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Bank with bic " + bic + " not exists."));
    }

    public List<BankDTO> getBankByNameContaining(String name) {
        return Optional.of(
                        bankRepository.findByBankNameContaining(name).stream()
                                .map(bankMapper::bankToBankDTO)
                                .collect(Collectors.toList()))
                .filter(list -> !list.isEmpty())
                .orElseThrow(
                        () ->
                                new ElementNotFoundException(
                                        "Bank with name " + name + " not found."));
    }

    public Bank addNewBank(BankDTO bank) {
        validate(bank);
        if (bankRepository.existsBankByBic(bank.getBic()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Bank with bic " + bank.getBic() + " already exists");

        Bank newBank = bankMapper.bankDTOToBank(bank);
        newBank.setUuid(UUID.randomUUID());
        bankRepository.save(newBank);
        paymentNetworkFeignClientInterface.registerNewBank(
                bankMapper.bankToPaymentNetworkBankDTO(newBank));

        return newBank;
    }

    public void deleteBank(UUID uuid) {
        Bank bank =
                bankRepository
                        .findBankByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                String.format(
                                                        "Bank with uuid %s not exists.", uuid)));
        paymentNetworkFeignClientInterface.unregisterBank(bank.getUuid());
        bankRepository.deleteById(bank.getId());
    }

    @Transactional
    public BankDTO updateBank(UUID uuid, BankDTO newBankEntity) {
        validate(newBankEntity);
        Bank existingBank =
                bankRepository
                        .findBankByUuid(uuid)
                        .orElseThrow(
                                () ->
                                        new ElementNotFoundException(
                                                "Bank with uuid " + uuid + " not exists."));

        if (bankRepository.existsBankByBic(newBankEntity.getBic()))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Bank with bic " + newBankEntity.getBic() + " already exists");

        bankMapper.mapToBank(newBankEntity, existingBank);

        bankRepository.save(existingBank);
        paymentNetworkFeignClientInterface.updateBank(
                uuid, bankMapper.bankToPaymentNetworkBankDTO(existingBank));

        return bankMapper.bankToBankDTO(existingBank);
    }

    public void validate(BankDTO bankDTO) {
        if (StringUtils.isBlank(bankDTO.getBankName()) || bankDTO.getBankName().length() > 255)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        if (StringUtils.isBlank(bankDTO.getBic()) || bankDTO.getBic().length() > 11)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid bic code");
        if (StringUtils.isBlank(bankDTO.getCountry()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid country name");
        if (StringUtils.isBlank(bankDTO.getCurrency()) || bankDTO.getCurrency().length() > 3)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid currency code");
    }
}
