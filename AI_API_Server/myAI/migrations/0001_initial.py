from django.db import migrations, models

class Migration(migrations.Migration):

    initial = True

    operations = [
        migrations.CreateModel(
            name='MyAiModel',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('title', models.IntegerField()),
                ('body', models.TextField()),
                ('VPpercent', models.FloatField()),
                ('answer', models.IntegerField()),
            ],
        ),
    ]
