using System;
using Microsoft.EntityFrameworkCore.ChangeTracking;
using Microsoft.EntityFrameworkCore.ValueGeneration;
using Snowflakes;

namespace AM_Chat_WebServer;

public class SnowflakeGenerator : ValueGenerator<long>
{
    public Snowflakes.SnowflakeGenerator Generator { get; set; }

    public SnowflakeGenerator()
    {
        var epoch = DateTimeOffset.FromUnixTimeSeconds(1746521241);
        var instance = 0;
        Generator = new SnowflakeGeneratorBuilder()
            .AddTimestamp(41, epoch, TimeSpan.TicksPerMillisecond)
            .AddConstant(1,instance)
            .AddSequenceForTimestamp(12)
            .Build();
        Instance = this;
    }

    public override long Next(EntityEntry entry)
    {
        return Generator.NewSnowflake();
    }

    public override bool GeneratesTemporaryValues { get; } = true;
    public static SnowflakeGenerator Instance { get; set; }
}