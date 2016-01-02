package me.kenzierocks.plugins.currencysnowmen.implementation;

import java.util.UUID;

import org.spongepowered.api.data.manipulator.mutable.DisplayNameData;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

public class CSUniqueAccount extends CSAccount implements UniqueAccount {

    private static Text mapIdentToText(Identifiable ident) {
        Text base = Text.of(ident.getUniqueId().toString());
        if (ident instanceof Player) {
            Player user = (Player) ident;
            // TODO when DisplayNameData is implemented swap
            base = user.get(DisplayNameData.class)
                    .flatMap(dnd -> dnd.displayName().getDirect()).orElse(base);
        } else if (ident instanceof User) {
            User user = (User) ident;
            base = Text.of(user.getName());
        } else if (ident instanceof Subject) {
            Subject user = (Subject) ident;
            base = Text.of(user.getIdentifier());
        }
        return base;
    }

    private final UUID uuid;

    public CSUniqueAccount(Identifiable ident) {
        super(ident.getUniqueId().toString(), mapIdentToText(ident));
        this.uuid = ident.getUniqueId();
    }

    public CSUniqueAccount(UUID uuid) {
        super(uuid.toString());
        this.uuid = uuid;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }

}
