/*
 * This file is part of Currency☃, licensed under the MIT License (MIT).
 *
 * Copyright (c) kenzierocks (Kenzie Togami) <http://kenzierocks.me>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Table;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import me.kenzierocks.plugins.currencysnowmen.ExtendedCurrency;

final class DataMapAdapter
        implements JsonSerializer<Table<Currency, Set<Context>, BigDecimal>>,
        JsonDeserializer<Table<Currency, Set<Context>, BigDecimal>>,
        InstanceCreator<Table<Currency, Set<Context>, BigDecimal>> {

    private static final class CTACollector implements
            Collector<Entry<Map<String, String>, BigDecimal>, Map<Set<Context>, BigDecimal>, Map<Set<Context>, BigDecimal>> {

        @Override
        public Supplier<Map<Set<Context>, BigDecimal>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<Set<Context>, BigDecimal>, Entry<Map<String, String>, BigDecimal>>
                accumulator() {
            return (map,
                    entry) -> map
                            .put(entry.getKey().entrySet().stream()
                                    .map(e -> new Context(e.getKey(),
                                            e.getValue()))
                            .collect(Collectors.toSet()), entry.getValue());
        }

        @Override
        public BinaryOperator<Map<Set<Context>, BigDecimal>> combiner() {
            return (a, b) -> {
                a.putAll(b);
                return a;
            };
        }

        @Override
        public Function<Map<Set<Context>, BigDecimal>, Map<Set<Context>, BigDecimal>>
                finisher() {
            return Function.identity();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return ImmutableSet.of(Characteristics.UNORDERED,
                    Characteristics.IDENTITY_FINISH);
        }

    }

    private static final Gson NORMAL_JSON =
            new GsonBuilder().enableComplexMapKeySerialization().create();
    @SuppressWarnings("serial")
    private static final Type DATA_TYPE =
            new TypeToken<Map<String, Map<Map<String, String>, BigDecimal>>>() {
            }.getType();

    @Override
    public Table<Currency, Set<Context>, BigDecimal> createInstance(Type type) {
        return HashBasedTable.create();
    }

    @Override
    public Table<Currency, Set<Context>, BigDecimal> deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
                    throws JsonParseException {
        System.err.println(json);
        Map<String, Map<Map<String, String>, BigDecimal>> data =
                NORMAL_JSON.fromJson(json, DATA_TYPE);
        Table<Currency, Set<Context>, BigDecimal> target =
                createInstance(typeOfT);
        data.forEach((str, map) -> {
            Currency currency =
                    CSEconomyService.INSTANCE.getCurrencyByIdentifier(str);
            Map<Set<Context>, BigDecimal> contextToAmount =
                    map.entrySet().stream().collect(new CTACollector());
            target.row(currency).putAll(contextToAmount);
        });
        return target;
    }

    @Override
    public JsonElement serialize(Table<Currency, Set<Context>, BigDecimal> src,
            Type typeOfSrc, JsonSerializationContext context) {
        Map<String, Map<Map<String, String>, BigDecimal>> data =
                new HashMap<>();
        src.rowMap().forEach((currency, contextToAmount) -> {
            String id = ((ExtendedCurrency) currency).getIdentifer();
            contextToAmount.forEach((contextSet, amount) -> {
                data.computeIfAbsent(id, k -> new HashMap<>())
                        .put(contextSet.stream().collect(Collectors
                                .toMap(Entry::getKey, Entry::getValue)),
                        amount);
            });
        });
        return NORMAL_JSON.toJsonTree(data, DATA_TYPE);
    }

}
