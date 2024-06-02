package com.unir.facets.utils;

public final class Consts {

    private Consts() {
        throw new IllegalStateException("Utility class");
    }

    //Nombres de campos
    public static final String FIELD_FIRST_NAME = "FirstName";
    public static final String FIELD_LAST_NAME = "LastName";
    public static final String FIELD_INTERESTS = "Interests";
    public static final String FIELD_AGE = "Age";
    public static final String FIELD_DATE_OF_JOINING = "DateOfJoining";
    public static final String FIELD_ADDRESS = "Address";
    public static final String FIELD_DESIGNATION = "Designation";
    public static final String FIELD_MARITAL_STATUS = "MaritalStatus";
    public static final String FIELD_SALARY = "Salary";
    public static final String FIELD_GENDER = "Gender";

    //Nombres de agregaciones
    public static final String AGG_KEY_RANGE_AGE = "ageValues";
    public static final String AGG_KEY_RANGE_AGE_0 = "-29";
    public static final String AGG_KEY_RANGE_AGE_1 = "29-33";
    public static final String AGG_KEY_RANGE_AGE_2 = "33-";

    public static final String AGG_KEY_RANGE_SALARY = "salaryValues";
    public static final String AGG_KEY_RANGE_SALARY_0 = "-62000";
    public static final String AGG_KEY_RANGE_SALARY_1 = "62000-68000";
    public static final String AGG_KEY_RANGE_SALARY_2 = "68000-";

    public static final String AGG_KEY_TERM_GENDER = "genderValues";

    public static final String AGG_KEY_TERM_DESIGNATION = "designationValues";

    public static final String AGG_KEY_TERM_MARITAL_STATUS = "civilStatusValues";

}
