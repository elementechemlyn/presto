/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.prestosql.operator.scalar.timestamptz;

import io.prestosql.spi.function.LiteralParameter;
import io.prestosql.spi.function.LiteralParameters;
import io.prestosql.spi.function.ScalarOperator;
import io.prestosql.spi.function.SqlType;
import io.prestosql.spi.type.LongTimestampWithTimeZone;
import io.prestosql.spi.type.StandardTypes;
import io.prestosql.type.Constraint;
import org.joda.time.DateTimeField;
import org.joda.time.chrono.ISOChronology;

import static io.prestosql.spi.function.OperatorType.ADD;
import static io.prestosql.spi.function.OperatorType.SUBTRACT;
import static io.prestosql.spi.type.DateTimeEncoding.packDateTimeWithZone;
import static io.prestosql.spi.type.DateTimeEncoding.unpackMillisUtc;
import static io.prestosql.spi.type.DateTimeEncoding.unpackZoneKey;
import static io.prestosql.type.DateTimes.PICOSECONDS_PER_MILLISECOND;
import static io.prestosql.type.DateTimes.roundToNearest;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public final class TimestampWithTimeZoneOperators
{
    private TimestampWithTimeZoneOperators() {}

    @ScalarOperator(ADD)
    public static final class TimestampPlusIntervalDayToSecond
    {
        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static long add(
                @LiteralParameter("p") long precision,
                @SqlType("timestamp(p) with time zone") long packedEpochMillis,
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval)
        {
            return packDateTimeWithZone(unpackMillisUtc(packedEpochMillis) + interval, unpackZoneKey(packedEpochMillis));
        }

        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static LongTimestampWithTimeZone add(
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp,
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval)
        {
            return LongTimestampWithTimeZone.fromEpochMillisAndFraction(timestamp.getEpochMillis() + interval, timestamp.getPicosOfMilli(), timestamp.getTimeZoneKey());
        }
    }

    @ScalarOperator(ADD)
    public static final class IntervalDayToSecondPlusTimestamp
    {
        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static long add(
                @LiteralParameter("p") long precision,
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval,
                @SqlType("timestamp(p) with time zone") long timestamp)
        {
            return TimestampPlusIntervalDayToSecond.add(precision, timestamp, interval);
        }

        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static LongTimestampWithTimeZone add(
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval,
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp)
        {
            return TimestampPlusIntervalDayToSecond.add(timestamp, interval);
        }
    }

    @ScalarOperator(ADD)
    public static final class TimestampPlusIntervalYearToMonth
    {
        private static final DateTimeField MONTH_OF_YEAR_UTC = ISOChronology.getInstanceUTC().monthOfYear();

        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static long add(
                @SqlType("timestamp(p) with time zone") long packedEpochMillis,
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval)
        {
            long epochMillis = unpackMillisUtc(packedEpochMillis);
            long result = MONTH_OF_YEAR_UTC.add(epochMillis, interval);

            return packDateTimeWithZone(result, unpackZoneKey(packedEpochMillis));
        }

        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static LongTimestampWithTimeZone add(
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp,
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval)
        {
            long epochMillis = timestamp.getEpochMillis();
            long result = MONTH_OF_YEAR_UTC.add(epochMillis, interval);

            return LongTimestampWithTimeZone.fromEpochMillisAndFraction(result, timestamp.getPicosOfMilli(), timestamp.getTimeZoneKey());
        }
    }

    @ScalarOperator(ADD)
    public static final class IntervalYearToMonthPlusTimestamp
    {
        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static long add(
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval,
                @SqlType("timestamp(p) with time zone") long timestamp)
        {
            return TimestampPlusIntervalYearToMonth.add(timestamp, interval);
        }

        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static LongTimestampWithTimeZone add(
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval,
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp)
        {
            return TimestampPlusIntervalYearToMonth.add(timestamp, interval);
        }
    }

    @ScalarOperator(SUBTRACT)
    public static final class TimestampMinusIntervalYearToMonth
    {
        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static long subtract(
                @SqlType("timestamp(p) with time zone") long timestamp,
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval)
        {
            return TimestampPlusIntervalYearToMonth.add(timestamp, -interval);
        }

        @LiteralParameters("p")
        @SqlType("timestamp(p) with time zone")
        public static LongTimestampWithTimeZone subtract(
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp,
                @SqlType(StandardTypes.INTERVAL_YEAR_TO_MONTH) long interval)
        {
            return TimestampPlusIntervalYearToMonth.add(timestamp, -interval);
        }
    }

    @ScalarOperator(SUBTRACT)
    public static final class TimestampMinusIntervalDayToSecond
    {
        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static long subtract(
                @LiteralParameter("p") long precision,
                @SqlType("timestamp(p) with time zone") long timestamp,
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval)
        {
            return TimestampPlusIntervalDayToSecond.add(precision, timestamp, -interval);
        }

        @LiteralParameters({"p", "u"})
        @SqlType("timestamp(u) with time zone")
        @Constraint(variable = "u", expression = "max(3, p)") // Interval is currently p = 3, so the minimum result precision is 3.
        public static LongTimestampWithTimeZone subtract(
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone timestamp,
                @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND) long interval)
        {
            return TimestampPlusIntervalDayToSecond.add(timestamp, -interval);
        }
    }

    @ScalarOperator(SUBTRACT)
    public static final class TimestampMinusTimestamp
    {
        @LiteralParameters("p")
        @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND)
        public static long subtract(
                @SqlType("timestamp(p) with time zone") long left,
                @SqlType("timestamp(p) with time zone") long right)
        {
            return unpackMillisUtc(left) - unpackMillisUtc(right);
        }

        @LiteralParameters("p")
        @SqlType(StandardTypes.INTERVAL_DAY_TO_SECOND)
        public static long subtract(
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone left,
                @SqlType("timestamp(p) with time zone") LongTimestampWithTimeZone right)
        {
            long interval = left.getEpochMillis() - right.getEpochMillis();

            int deltaPicos = left.getPicosOfMilli() - right.getPicosOfMilli();
            if (deltaPicos < 0 && roundToNearest(-deltaPicos, PICOSECONDS_PER_MILLISECOND) == PICOSECONDS_PER_MILLISECOND) {
                // borrow and round
                interval--;
            }
            else if (deltaPicos > 0 && roundToNearest(deltaPicos, PICOSECONDS_PER_MILLISECOND) == PICOSECONDS_PER_MILLISECOND) {
                // round up
                interval++;
            }

            return interval;
        }
    }
}
