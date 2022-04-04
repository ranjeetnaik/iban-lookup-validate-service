package com.ibanlookup.service.impl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ibanlookup.datapoll.PollS3Bucket;
import com.ibanlookup.exception.IBANLookupException;
import com.ibanlookup.model.IbanDataMap;
import com.ibanlookup.model.IbanPlusBank;
import com.ibanlookup.model.IbanPlusCountry;
import com.ibanlookup.model.IbanPlusData;
import com.ibanlookup.model.IbanResponse;
import com.ibanlookup.service.IbanLookupService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class IbanLookupServiceImpl implements IbanLookupService {

    private static final String TWO_ZERO_KEY = "00";
    private static final BigInteger NINETY_EIGHT = BigInteger.valueOf(98L);
    private static final BigInteger NINETY_SEVEN = BigInteger.valueOf(97L);
    private static final int INDEX_ADJUST_ONE = 1;
    private static final int FIFTY_FIVE = 55;
    private static final String IS_SEPA_MEMBER = "Y";
    private static final Pattern PATTERN_IBAN = Pattern.compile("^([a-zA-Z]{2})(\\d{2})([a-zA-Z0-9]{0,30})$");

    private final PollS3Bucket pollS3Bucket;

    @Override
    public IbanResponse validateAndFetchIbanInformation(String iban) {
        if (pollS3Bucket.getIbanDataMap() == null || CollectionUtils.isEmpty(pollS3Bucket.getIbanDataMap().getIbanPlusCountryList())) {
            throw new IBANLookupException("Iban plus data is not available or loaded in service", ErrorCode.SERVICE_DATA_NOT_AVAILABLE_OR_LOADED);
        }
        try {
            validateIban(iban);
            var dataMap = pollS3Bucket.getIbanDataMap();
            var countryCode = iban.substring(0, 2);
            //validate country code and iban length
            var ibanPlusCountry = validateCountryCodeIbanIdLengthAndReturnIbanPlusCountry(iban, countryCode, dataMap);
            //Check modulo 97
            checkModulo97(iban);
            //iban national id is same as bank code
            int startPositionForBankCodeExtract = ibanPlusCountry.getBankIdentifierPosition() - INDEX_ADJUST_ONE;
            var ibanNationalId = iban.substring(startPositionForBankCodeExtract, (startPositionForBankCodeExtract + ibanPlusCountry.getIbanNationalIdLength()));

            //check if IBAN is in exclusion list
            if (checkIfIbanExclusionList(countryCode, ibanNationalId)) {
                log.error("iban is invalid its found in exclusion list,  country code:{} and bank code(ibanNationalId):{} ", countryCode, ibanNationalId);
                throw new IBANLookupException("Invalid iban, its found in iban exclusion list", ErrorCode.IBAN_FOUND_IN_EXCLUSION_LIST);
            }

            var ibanPlusBank = checkIbanPlusBankData(dataMap, countryCode, ibanNationalId);
            return buildResponse(countryCode, ibanPlusCountry, ibanNationalId, ibanPlusBank);
        } catch (IBANLookupException ibanLookupException) {
            // ErrorCode.IBAN_TOTAL_ID_LENGTH_NOT_MATCHING & ErrorCode.CHECKSUM_VALIDATION_FAILED are logged as warning (data validations) except
            // all other exceptions logged as error.
            boolean logError = !ErrorCode.IBAN_TOTAL_ID_LENGTH_NOT_MATCHING.equals(ibanLookupException.getErrorCode()) &&
                    !ErrorCode.CHECKSUM_VALIDATION_FAILED.equals(ibanLookupException.getErrorCode());
            if (logError) {
                log.error("Exception while validating iban: {} ", iban, ibanLookupException);
            }
            return IbanResponse.builder().ibanValid(Boolean.FALSE).reason(ibanLookupException.getErrorCode() != null ? ibanLookupException.getErrorCode().getDescription() : "").build();
        }
    }

    private void validateIban(String iban) {
        if (StringUtils.isBlank(iban)) {
            throw new IBANLookupException("Iban is null or blank", ErrorCode.IBAN_IS_NULL_OR_BLANK);
        }
        if (!PATTERN_IBAN.matcher(iban).matches()) {
            throw new IBANLookupException("Iban pattern matching failed", ErrorCode.IBAN_PATTERN_MATCHING_FAILED);
        }
    }

    private IbanPlusBank checkIbanPlusBankData(IbanDataMap dataMap, String countryCode, String ibanNationalId) {
        Optional<IbanPlusBank> ibanPlusBankOptional = dataMap.getIbanPlusBanksList().stream().filter(ibanPlusBankrecord -> ibanPlusBankrecord.getCountryCode().equals(countryCode)
                && ibanPlusBankrecord.getBankCode().equals(ibanNationalId)).findFirst();
        var ibanPlusBank = ibanPlusBankOptional.orElse(null);
        if (ibanPlusBank == null) {
            log.warn("ibanplus bank data not found for country code:{} and bank code(ibanNationalId):{} ", countryCode, ibanNationalId);
        }
        return ibanPlusBank;
    }

    private IbanResponse buildResponse(String countryCode, IbanPlusCountry ibanPlusCountry, String ibanNationalId, IbanPlusBank ibanPlusBank) {
        IbanPlusData ibanPlusData = null;
        if (ibanPlusBank != null) {
            ibanPlusData = IbanPlusData.builder().countryCode(countryCode).bic(ibanPlusBank.getIbanBIC()).
                    institutionName(ibanPlusBank.getBankName()).nationalId(ibanNationalId).
                    recordKey(ibanPlusBank.getRecordKey()).sepa(StringUtils.isNotBlank(ibanPlusCountry.getSepa()) && ibanPlusCountry.getSepa().equals(IS_SEPA_MEMBER) ? Boolean.TRUE : Boolean.FALSE).build();
        }
        return IbanResponse.builder().ibanValid(Boolean.TRUE
        ).ibanPlusData(ibanPlusData).build();
    }

    /**
     * 1. validate country code
     * 2. validate iban id length
     * 3. return Iban Plus Country data
     *
     * @param iban        IBAN account number
     * @param countryCode country code
     * @param dataMap     iban data map which contains list of ibanplus country bank and exclusion
     * @return IbanPlusCountry
     */
    private IbanPlusCountry validateCountryCodeIbanIdLengthAndReturnIbanPlusCountry(String iban, String countryCode, IbanDataMap dataMap) {
        Optional<IbanPlusCountry> ibanPlusCountryOptional = dataMap.getIbanPlusCountryList().stream().
                filter(ibanPlusCountry -> ibanPlusCountry.getCountryCode().equals(countryCode)).findFirst();

        var ibanPlusCountry = ibanPlusCountryOptional.orElseThrow(() -> {
            log.error("Country code {} not found", countryCode);
            throw new IBANLookupException("Iban plus country data not found for requested iban", ErrorCode.COUNTRY_DATA_NOT_FOUND);
        });
        if (ibanPlusCountry.getIbanIdLength() != iban.length()) {
            log.warn("Expected IBAN total id length {} is not matching with iban length {} for iban: {}", ibanPlusCountry.getIbanIdLength(), iban.length(), iban);
            throw new IBANLookupException("Iban plus total id length is not matching for requested iban", ErrorCode.IBAN_TOTAL_ID_LENGTH_NOT_MATCHING);
        }
        return ibanPlusCountry;
    }

    /**
     * Traverse the exclusion list, if the input iban is in exlusion list then
     * throw the exception with message "invalid iban, iban found in the exclusion list"
     *
     * @param countryCode    iban country code
     * @param ibanNationalId iban national Id
     */
    private boolean checkIfIbanExclusionList(String countryCode, String ibanNationalId) {
        boolean isExclusionFound;
        var ibanDataMap = pollS3Bucket.getIbanDataMap();
        var localDateTime = LocalDateTime.now();
        if (ibanDataMap.getExclusionsList() != null) {
            isExclusionFound = ibanDataMap.getExclusionsList().stream().anyMatch(exclusions -> exclusions.getCountryCode().equals(countryCode)
                    && exclusions.getIbanNationalId().equals(ibanNationalId) &&
                    (StringUtils.isBlank(exclusions.getValidFrom()) || LocalDateTime.parse(exclusions.getValidFrom()).isBefore(localDateTime)));
        } else {
            throw new IBANLookupException("IbanData map exclusion list is null", ErrorCode.EXCLUSION_LIST_NOT_FOUND);
        }
        return isExclusionFound;
    }

    /**
     * algorithm starts by moving the 4 first characters (country code and checksum key) at the end of the IBAN and set the key to 2 digits 00.
     * Example: An invented IBAN: BA51 1234 5678 90DE F123,
     * it becomes 1234 5678 90DE F123 BA00
     * Replace each letter in the IBAN by its rank in the alphabet + 9. So A=10, B=11, C=12, etc. Z=35
     * Example: IBAN becomes 1234567890131415123111000
     * Calculate the value of this big number modulo 97 and subtract the result to 98 in order to get the control key.
     * Example: 1234567890131415123101100 mod 97 = 47 and 98-47 = 51. The checksum key is 51.
     * Check if the two 2 characters after the country code are 51, if yes the IBAN passes validation.
     * Check modulo 97
     *
     * @param iban iban account number
     */
    private void checkModulo97(final String iban) {
        var rearrangedIban = iban.substring(4).concat(iban.substring(0, 2)).concat(TWO_ZERO_KEY);
        var actualCheckSum = iban.substring(2, 4);
        var ibanCharArray = rearrangedIban.toUpperCase().toCharArray();
        var convertedCharBuilder = new StringBuilder();
        for (var accountChar : ibanCharArray) {
            if (Character.isLetter(accountChar)) {
                //a-z,A-Z to be replace by 10 to 35 , ascii value of upper case char is subtracted by 55
                var convertedValue = accountChar - FIFTY_FIVE;
                convertedCharBuilder.append(convertedValue);
            } else {
                convertedCharBuilder.append(accountChar);
            }
        }
        var calculatedChecksum = NINETY_EIGHT.subtract(new BigInteger(convertedCharBuilder.toString()).mod(NINETY_SEVEN));
        var calculatedChecksumWithLeftPad = StringUtils.leftPad(String.valueOf(calculatedChecksum), 2, '0');
        if (!actualCheckSum.equals(calculatedChecksumWithLeftPad)) {
            log.warn("Checksum validation failed for IBAN : {} actual checksum: {} and calculated checksum: {}", iban, actualCheckSum, calculatedChecksumWithLeftPad);
            throw new IBANLookupException("Iban plus checksum validation failed for requested iban", ErrorCode.CHECKSUM_VALIDATION_FAILED);
        }
    }
}