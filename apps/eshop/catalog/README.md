# eShop Catalog — Graftcode

Original project:
https://github.com/dotnet/eShop

A .NET class library that exposes the eShop catalog, basket, and ordering as a Graftcode module. PostgreSQL persists brands, types, items, stock, baskets, and orders. There is no REST API or generated client to maintain. There are no user accounts and no payments: a buyer id is a string the caller passes in, and checkout confirms stock immediately.

Seed names, descriptions, brands, and types come from the [dotnet/eShop](https://github.com/dotnet/eShop) catalog (`src/Catalog.API/Setup/catalog.json`), which is licensed under the MIT License.

## What was changed

- Exposed `Catalog`, `Basket`, and `Ordering` as one Graftcode module
- Replaced the original HTTP API with Graftcode Gateway
- Kept PostgreSQL for brands, types, items, stock, baskets, and orders
- Dropped user accounts and payments: a buyer id is a string, and checkout confirms stock immediately

## Original architecture

dotnet/eShop is a larger .NET store. This scenario keeps the catalog slice: brands, types, items, and the eight-item seed from `Catalog.API`. Identity, payment, the event bus, pictures, Blazor, and the mobile client stay in the original project.

## Graftcode architecture

`src/CatalogService` is the host. Graftcode Gateway serves calls on port 80 (`ws://localhost/ws`) and Graftcode Vision on port 81. PostgreSQL on port 5432 stores the catalog, baskets, and orders. `src/CatalogConsumer` is a separate .NET process that installs the graft and calls it.

## Run

Prerequisites:

- [Docker](https://docs.docker.com/get-docker/) with Docker Compose installed and running
- [.NET SDK 9](https://dotnet.microsoft.com/download)

From `apps/eshop/catalog/src`:

```bash
docker compose up --build -d
docker compose ps
```

Compose starts PostgreSQL on port 5432 and waits for it to become healthy before starting the backend. Port 80 serves calls (`ws://localhost/ws`). Port 81 serves Graftcode Vision.

The schema and the eight-item seed are in [`src/database/init.sql`](src/database/init.sql). PostgreSQL stores data in the `postgres-data` named volume, so catalog changes, baskets, orders, and stock survive container restarts. Stop the containers with `docker compose down`. To delete all data and recreate the seed on the next start, use:

```bash
docker compose down -v
```

Wait until the install command is available:

```bash
curl http://localhost:80/nuget
```

Open Graftcode Vision at [http://localhost:81/GV](http://localhost:81/GV). You should see `Catalog`, `Basket`, and `Ordering`. The gateway build used while writing this sample also serves that page at [http://localhost:80/GV](http://localhost:80/GV). If `docker compose logs backend` prints a different Vision URL, use that one.

The registry GUID changes every time the container starts. Copy the current GUID and the `dotnet add package` command from `http://localhost:80/nuget` (Vision, Configuration tab). Do not reuse a GUID from an earlier run.

## Try it

The feed at `grft.dev` serves grafts only. [`src/CatalogConsumer/NuGet.config`](src/CatalogConsumer/NuGet.config) maps `graft.nuget.*` and `Hypertube.*` to that feed and everything else to nuget.org. Replace `YOUR_GUID` in that file with the GUID from the install command.

From `src/CatalogConsumer`, paste the command from `http://localhost:80/nuget`. It looks like this (the GUID and version come from that response):

```bash
dotnet add package graft.nuget.catalogservice -v 1.0.0 --source https://grft.dev/YOUR_GUID__free
dotnet run
```

`src/CatalogConsumer/Program.cs` points the graft at the local gateway, loads the first catalog item, asks for a missing one, then checks out a basket and tries to buy the sold-out item:

```csharp
using graft.nuget.CatalogService;

GraftConfig.Host = "ws://localhost/ws";
GraftConfig.Stateless = true;

var item = Catalog.GetItem(1);
Console.WriteLine($"Getting item 1: {item.Name}");

try
{
    Catalog.GetItem(999);
}
catch (Exception ex)
{
    Console.WriteLine($"Getting item 999: {ex.Message}");
}

var basket = Basket.UpdateBasket("demo", [1], [1]);
Console.WriteLine($"Basket: {basket.ProductName} x {basket.Quantity}");

var address = new AddressDto
{
    Street = "1 Adventure Works Way",
    City = "Redmond",
    State = "WA",
    Country = "USA",
    ZipCode = "98052"
};

var order = Ordering.Checkout("demo", address);
Console.WriteLine($"Order {order.Id}: {order.Status}");

var shipped = Ordering.Ship(order.Id);
Console.WriteLine($"Order {order.Id}: {shipped.Status}");

try
{
    Basket.UpdateBasket("demo", [6], [1]);
    Ordering.Checkout("demo", address);
}
catch (Exception ex)
{
    Console.WriteLine($"Checkout item 6: {ex.Message}");
}
```

`GraftConfig.Stateless = true` returns the whole DTO in one round trip.

`UpdateBasket` copies the current name, price, and picture from the catalog. The returned basket exposes the first line as `ProductName` and `Quantity`. `Checkout` reads the price again, removes stock for every line or leaves the catalog and the basket unchanged, then stores an order already in `StockConfirmed`. There is no `Paid` status. `Ship` moves that order to `Shipped`. `Cancel` is allowed only from `StockConfirmed` and puts the units back.

Expected output:

```text
Getting item 1: Wanderer Black Hiking Boots
Getting item 999: Item with id 999 not found.
Basket: Wanderer Black Hiking Boots x 1
Order 1: StockConfirmed
Order 1: Shipped
Checkout item 6: Empty stock, product item Carbon Fiber Trekking Poles is sold out
```

The first line is the seeded item `Id` 1. A missing item and an empty warehouse come back as a plain `Exception`; the message is preserved. Item 6 is seeded with no stock, and that failed checkout leaves the basket in place.

## Graft

```text
Catalog.ListItems(...)
Catalog.GetItem(...)
Catalog.GetItemsByIds(...)
Catalog.ListBrands(...)
Catalog.ListTypes(...)
Catalog.GetFacets(...)
Catalog.CreateItem(...)
Catalog.UpdateItem(...)
Catalog.DeleteItem(...)
Catalog.RemoveStock(...)

Basket.GetBasket(...)
Basket.UpdateBasket(...)
Basket.DeleteBasket(...)

Ordering.Checkout(...)
Ordering.GetOrder(...)
Ordering.ListOrders(...)
Ordering.Ship(...)
Ordering.Cancel(...)
```

## Dev container

[`src/.devcontainer/devcontainer.json`](src/.devcontainer/devcontainer.json) uses the .NET SDK 9 image and installs Graftcode Gateway with:

```bash
curl -fsSL grft.dev/get/gg | sh
```

## Follow-up

Basket and ordering in this module skip identity, payment, and the event bus. Still to come, and not in this repository:

- pgvector and semantic search
- the event bus (`ProductPriceChanged`)
- binary item pictures
- the rest of eShop (identity, payment, Blazor, mobile)
- Academy hub UI, if it only lists the numbered Quick Start courses
