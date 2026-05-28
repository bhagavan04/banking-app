package net.javaguides.banking_app.service.impl;

import net.javaguides.banking_app.dto.AccountDto;
import net.javaguides.banking_app.dto.TransferFundDto;
import net.javaguides.banking_app.entity.Account;
import net.javaguides.banking_app.entity.Transaction;
import net.javaguides.banking_app.mapper.AccountMapper;
import net.javaguides.banking_app.repository.AccountRepository;
import net.javaguides.banking_app.repository.TransactionRespository;
import net.javaguides.banking_app.service.AccountService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountServiceImpl implements AccountService {

    private AccountRepository accountRepository;

    private TransactionRespository transactionRespository;

    private static final String TRANSACTION_TYPE="DEPOSIT";
    private static final String TRANSACTION_TYPE_WITHDRAW="WITHDRAW";
    public AccountServiceImpl(AccountRepository accountRepository,
                              TransactionRespository transactionRespository) {
        this.accountRepository = accountRepository;
        this.transactionRespository=transactionRespository;
    }

    @Override
    public AccountDto createAccount(AccountDto accountDto) {
        Account account= AccountMapper.mapToAccount(accountDto);
        Account savedAccount=accountRepository.save(account);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto getAccountById(Long id) {
       Account account= accountRepository
               .findById(id)
               .orElseThrow(()-> new RuntimeException("id doest not exist"));
        return AccountMapper.mapToAccountDto(account);
    }

    @Override
    public AccountDto deposit(Long id, double amount) {

        Account account= accountRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("id doest not exist"));
        double total=account.getBalance()+amount;
        account.setBalance(total);
        Account savedAccount=accountRepository.save(account);
        Transaction transaction=new Transaction();

        transaction.setAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE );
        transaction.setTimeStamp(LocalDateTime.now());

         transactionRespository.save(transaction);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public AccountDto withdraw(Long id, double amount) {

        Account account= accountRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("id doest not exist"));
        if(account.getBalance()<amount){
            throw new RuntimeException("insufficient balance");
        }
        double total=account.getBalance()-amount;
        account.setBalance(total);
        Account savedAccount=accountRepository.save(account);
        Transaction transaction=new Transaction();

        transaction.setAccountId(id);
        transaction.setAmount(amount);
        transaction.setTransactionType(TRANSACTION_TYPE_WITHDRAW );
        transaction.setTimeStamp(LocalDateTime.now());

        transactionRespository.save(transaction);
        return AccountMapper.mapToAccountDto(savedAccount);
    }

    @Override
    public List<AccountDto> getAllAccounts() {

        List<Account> accounts=accountRepository.findAll();
        return accounts.stream().map((account) ->AccountMapper.mapToAccountDto(account))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteAccount(Long id) {

        Account account= accountRepository
                .findById(id)
                .orElseThrow(()-> new RuntimeException("id doest not exist"));
        accountRepository.deleteById(id);
    }

    @Override
    public void transferFunds(TransferFundDto transferFundDto) {
        Account fromAccount= accountRepository
                .findById(transferFundDto.fromAccountId())
                .orElseThrow(()-> new RuntimeException("id doest not exist"));

        //retrieve the acc to which we send the amount
        Account toAccount= accountRepository
                .findById(transferFundDto.toAccountId())
                .orElseThrow(()-> new RuntimeException("id doest not exist"));

        //debit
        fromAccount.setBalance(fromAccount.getBalance()-transferFundDto.amount());

        //credit
        toAccount.setBalance(toAccount.getBalance()+transferFundDto.amount());

         accountRepository.save(fromAccount);
         accountRepository.save(toAccount);
    }
}
