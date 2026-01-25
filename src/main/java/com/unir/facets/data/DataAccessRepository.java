package com.unir.facets.data;

import com.unir.facets.data.model.Employee;
import com.unir.facets.controller.model.AggregationDetails;
import com.unir.facets.controller.model.EmployeesQueryResponse;
import com.unir.facets.utils.Consts;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder;
import org.opensearch.data.client.orhlc.OpenSearchAggregations;
import org.opensearch.data.core.OpenSearchOperations;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.index.query.RangeQueryBuilder;
import org.opensearch.search.aggregations.Aggregation;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.opensearch.search.aggregations.bucket.filter.ParsedFilters;
import org.opensearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Slf4j
public class DataAccessRepository {

    // Esta clase (y bean) es la unica que usan directamente los servicios para
    // acceder a los datos.
    private final EmployeeRepository employeeRepository;
    private final ElasticsearchOperations openSearchClient;

    private final String[] address_fields = {"Address", "Address._2gram", "Address._3gram"};

    @SneakyThrows
    public EmployeesQueryResponse findProducts(
            List<String> genderValues,
            List<String> designationValues,
            List<String> civilStatusValues,
            List<String> ageValues,
            List<String> salaryValues,
            String name,
            String address,
            String page) {

        BoolQueryBuilder querySpec = QueryBuilders.boolQuery();

        // Si el usuario ha seleccionado algun valor relacionado con el genero, lo añadimos a la query
        if (genderValues != null && !genderValues.isEmpty()) {
            genderValues.forEach(
                    gender -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_GENDER, gender))
            );
        }

        // Si el usuario ha seleccionado algun valor relacionado con el nombre, lo añadimos a la query
        if (!StringUtils.isEmpty(name)) {
            querySpec.must(QueryBuilders.matchQuery(Consts.FIELD_FIRST_NAME, name));
        }

        // Si el usuario ha seleccionado algun valor relacionado con la direccion, lo añadimos a la query
        if (!StringUtils.isEmpty(address)) {
            querySpec.must(QueryBuilders.multiMatchQuery(address, address_fields).type(MultiMatchQueryBuilder.Type.BOOL_PREFIX));
        }

        // Si el usuario ha seleccionado algun valor relacionado con la designacion, lo añadimos a la query
        if (designationValues != null && !designationValues.isEmpty()) {
            designationValues.forEach(
                    designation -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_DESIGNATION, designation))
            );
        }

        // Si el usuario ha seleccionado algun valor relacionado con el estado civil, lo añadimos a la query
        if (civilStatusValues != null && !civilStatusValues.isEmpty()) {
            civilStatusValues.forEach(
                    civilStatus -> querySpec.must(QueryBuilders.termQuery(Consts.FIELD_MARITAL_STATUS, civilStatus))
            );
        }

        // Si el usuario ha seleccionado algun valor relacionado con la edad, lo añadimos a la query
        if (ageValues != null && !ageValues.isEmpty()) {
            ageValues.forEach(
                    age -> {
                        String[] ageRange = age != null && age.contains("-") ? age.split("-") : new String[]{};

                        if (ageRange.length == 2) {
                            if ("".equals(ageRange[0])) {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_AGE).to(ageRange[1]).includeUpper(false));
                            } else {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_AGE).from(ageRange[0]).to(ageRange[1]).includeUpper(false));
                            }
                        } if (ageRange.length == 1) {
                            querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_AGE).from(ageRange[0]));
                        }
                    }
            );
        }

        // Si el usuario ha seleccionado algun valor relacionado con el salario, lo añadimos a la query
        if (salaryValues != null && !salaryValues.isEmpty())
            salaryValues.forEach(
                    salary -> {
                        String[] salaryRange = salary != null && salary.contains("-") ? salary.split("-") : new String[]{};

                        if (salaryRange.length == 2) {
                            if ("".equals(salaryRange[0])) {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_SALARY).to(salaryRange[1]).includeUpper(false));
                            } else {
                                querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_SALARY).from(salaryRange[0]).to(salaryRange[1]).includeUpper(false));
                            }
                        } if (salaryRange.length == 1) {
                            querySpec.must(QueryBuilders.rangeQuery(Consts.FIELD_SALARY).from(salaryRange[0]));
                        }
                    }
            );

        //Si no se ha seleccionado ningun filtro, se añade un filtro por defecto para que la query no sea vacia
        if(!querySpec.hasClauses()) {
            querySpec.must(QueryBuilders.matchAllQuery());
        }

        //Construimos la query
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(querySpec);

        //Se incluyen las Agregaciones
        //Agregaciones de término para genero, designacion y estado civil
        //Agregaciones de filtros para edad y salario (workaround para bug de OpenSearch con múltiples range aggregations)
        nativeSearchQueryBuilder.withAggregations(
                AggregationBuilders.terms(Consts.AGG_KEY_TERM_GENDER).field(Consts.FIELD_GENDER).size(10000),
                AggregationBuilders.terms(Consts.AGG_KEY_TERM_DESIGNATION).field(Consts.FIELD_DESIGNATION).size(10000),
                AggregationBuilders.terms(Consts.AGG_KEY_TERM_MARITAL_STATUS).field(Consts.FIELD_MARITAL_STATUS).size(10000),
                // Usar filters aggregation en lugar de range para evitar bug de OpenSearch
                AggregationBuilders.filters(Consts.AGG_KEY_RANGE_AGE,
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_AGE_0, QueryBuilders.rangeQuery(Consts.FIELD_AGE).lt(29)),
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_AGE_1, QueryBuilders.rangeQuery(Consts.FIELD_AGE).gte(29).lt(33)),
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_AGE_2, QueryBuilders.rangeQuery(Consts.FIELD_AGE).gte(33))
                ),
                AggregationBuilders.filters(Consts.AGG_KEY_RANGE_SALARY,
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_SALARY_0, QueryBuilders.rangeQuery(Consts.FIELD_SALARY).lt(62000)),
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_SALARY_1, QueryBuilders.rangeQuery(Consts.FIELD_SALARY).gte(62000).lt(68000)),
                        new FiltersAggregator.KeyedFilter(Consts.AGG_KEY_RANGE_SALARY_2, QueryBuilders.rangeQuery(Consts.FIELD_SALARY).gte(68000))
                )
        );


        //Se establece un maximo de 5 resultados, va acorde con el tamaño de la pagina
        nativeSearchQueryBuilder.withMaxResults(5);

        //Podemos paginar los resultados en base a la pagina que nos llega como parametro
        //El tamaño de la pagina es de 5 elementos (pero el propio llamante puede cambiarlo si se habilita en la API)
        int pageInt = Integer.parseInt(page);
        if (pageInt >= 0) {
            nativeSearchQueryBuilder.withPageable(PageRequest.of(pageInt,5));
        }

        //Se construye la query
        Query query = nativeSearchQueryBuilder.build();
        // Se realiza la busqueda
        SearchHits<Employee> result = openSearchClient.search(query, Employee.class);
        return new EmployeesQueryResponse(getResponseEmployees(result), getResponseAggregations(result));
    }

    /**
     * Metodo que convierte los resultados de la busqueda en una lista de empleados.
     * @param result Resultados de la busqueda.
     * @return Lista de empleados.
     */
    private List<Employee> getResponseEmployees(SearchHits<Employee> result) {
        return result.getSearchHits().stream().map(SearchHit::getContent).toList();
    }

    /**
     * Metodo que convierte las agregaciones de la busqueda en una lista de detalles de agregaciones.
     * Se ha de tener en cuenta que el tipo de agregacion puede ser de tipo rango o de tipo termino.
     * @param result Resultados de la busqueda.
     * @return Lista de detalles de agregaciones.
     */
    private Map<String, List<AggregationDetails>> getResponseAggregations(SearchHits<Employee> result) {

        //Mapa de detalles de agregaciones
        Map<String, List<AggregationDetails>> responseAggregations = new HashMap<>();

        //Recorremos las agregaciones
        if (result.hasAggregations()) {
            OpenSearchAggregations aggregations = (OpenSearchAggregations) result.getAggregations();
            Map<String, Aggregation> aggs = Objects.requireNonNull(aggregations).aggregations().asMap();

            //Recorremos las agregaciones
            aggs.forEach((key, value) -> {

                //Si no existe la clave en el mapa, la creamos
                if(!responseAggregations.containsKey(key)) {
                    responseAggregations.put(key, new LinkedList<>());
                }

                //Si la agregacion es de tipo termino, recorremos los buckets
                if (value instanceof ParsedStringTerms parsedStringTerms) {
                    parsedStringTerms.getBuckets().forEach(bucket ->
                        responseAggregations.get(key).add(new AggregationDetails(bucket.getKey().toString(), (int) bucket.getDocCount()))
                    );
                }

                //Si la agregacion es de tipo filters (usado como workaround para range), recorremos los buckets
                if (value instanceof ParsedFilters parsedFilters) {
                    parsedFilters.getBuckets().forEach(bucket ->
                        responseAggregations.get(key).add(new AggregationDetails(bucket.getKeyAsString(), (int) bucket.getDocCount()))
                    );
                }
            });
        }
        return responseAggregations;
    }
}
