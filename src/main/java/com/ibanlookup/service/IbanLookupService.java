package com.ibanlookup.service;

import com.ibanlookup.model.IbanResponse;


public interface IbanLookupService {
    IbanResponse validateAndFetchIbanInformation(String iban);
}