package com.unir.facets.model.db;

import com.unir.facets.utils.Consts;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Document(indexName = "employees", createIndex = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Employee {
	
	@Id
	private String id;
	
	@Field(type = FieldType.Text, name = Consts.FIELD_FIRST_NAME)
	private String firstName;

	@Field(type = FieldType.Text, name = Consts.FIELD_LAST_NAME)
	private String lastName;

	@Field(type = FieldType.Text, name = Consts.FIELD_INTERESTS)
	private String interests;
	
	@Field(type = FieldType.Integer, name = Consts.FIELD_AGE)
	private String age;

	@Field(type = FieldType.Date, format = DateFormat.date, name = Consts.FIELD_DATE_OF_JOINING)
	private LocalDate dateOfJoining;
	
	@Field(type = FieldType.Search_As_You_Type, name = Consts.FIELD_ADDRESS)
	private String address;
	
	@Field(type = FieldType.Keyword, name = Consts.FIELD_DESIGNATION)
	private String designation;

	@Field(type = FieldType.Keyword, name = Consts.FIELD_GENDER)
	private String gender;

	@Field(type = FieldType.Keyword, name = Consts.FIELD_MARITAL_STATUS)
	private String maritalStatus;

	@Field(type = FieldType.Double, name = Consts.FIELD_SALARY)
	private Double salary;

}
